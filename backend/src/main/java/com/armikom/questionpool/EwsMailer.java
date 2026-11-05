package com.armikom.questionpool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Sends mail through Exchange Web Services (EWS) with NTLM login. For on-premises Exchange servers whose SMTP ports
 * are closed to the outside but whose web address (Outlook on the web) is open, for example mail.thk.edu.tr.
 * Uses HttpURLConnection because java.net.http.HttpClient has no NTLM support.
 */
@Component
public class EwsMailer {

	private final String url;
	private final String username;
	private final String password;

	public EwsMailer(AppProps props, @Value("${spring.mail.username:}") String username,
			@Value("${spring.mail.password:}") String password) {
		this.url = props.ewsUrl();
		this.username = username;
		this.password = password;
		if (isConfigured()) {
			// Exchange offers Negotiate (Kerberos) first; outside the school domain only NTLM can work.
			System.setProperty("http.auth.preference", "NTLM");
		}
	}

	public boolean isConfigured() {
		return url != null && !url.isBlank() && !username.isBlank();
	}

	public void send(String to, String subject, String text) throws IOException {
		String body = """
				<?xml version="1.0" encoding="utf-8"?>
				<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
				 xmlns:t="http://schemas.microsoft.com/exchange/services/2006/types"
				 xmlns:m="http://schemas.microsoft.com/exchange/services/2006/messages">
				<soap:Header><t:RequestServerVersion Version="Exchange2013"/></soap:Header>
				<soap:Body><m:CreateItem MessageDisposition="SendOnly"><m:Items><t:Message>
				<t:Subject>%s</t:Subject>
				<t:Body BodyType="Text">%s</t:Body>
				<t:ToRecipients><t:Mailbox><t:EmailAddress>%s</t:EmailAddress></t:Mailbox></t:ToRecipients>
				</t:Message></m:Items></m:CreateItem></soap:Body></soap:Envelope>"""
				.formatted(xml(subject), xml(text), xml(to));

		HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();
		con.setAuthenticator(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password.toCharArray());
			}
		});
		con.setConnectTimeout(10_000);
		con.setReadTimeout(30_000);
		con.setRequestMethod("POST");
		con.setDoOutput(true);
		con.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
		try (OutputStream out = con.getOutputStream()) {
			out.write(body.getBytes(StandardCharsets.UTF_8));
		}
		int status = con.getResponseCode();
		String response;
		try (InputStream in = status < 400 ? con.getInputStream() : con.getErrorStream()) {
			response = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
		if (status != 200 || !response.contains("ResponseClass=\"Success\"")) {
			throw new IOException("EWS send failed: HTTP " + status + " " + firstError(response));
		}
	}

	private static String firstError(String response) {
		int i = response.indexOf("<m:MessageText>");
		if (i < 0) {
			return "";
		}
		int j = response.indexOf("</m:MessageText>", i);
		return j < 0 ? "" : response.substring(i + 15, j);
	}

	static String xml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
}
