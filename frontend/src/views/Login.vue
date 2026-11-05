<script setup>
import { ref } from 'vue'
import { useRoute } from 'vue-router'
import { post } from '../api'

const token = useRoute().query.token
const email = ref('')
const sent = ref(false)
const onRoster = ref(true)
const busy = ref(false)
const error = ref('')

async function requestLink() {
  busy.value = true
  error.value = ''
  try {
    const res = await post('/auth/request', { email: email.value })
    onRoster.value = res.on_roster
    sent.value = true
  } catch (e) {
    error.value = e.message
  }
  busy.value = false
}

// The token is used only when the student taps the button, so mail scanners that open links cannot use it up.
async function signIn() {
  busy.value = true
  error.value = ''
  try {
    await post('/auth/verify', { token })
    location.assign('/')
  } catch (e) {
    error.value = e.message
    busy.value = false
  }
}
</script>

<template>
  <div class="card" style="max-width: 460px; margin: 40px auto">
    <h1>SENG 403 Question Pool</h1>

    <template v-if="token">
      <p>Tap the button to finish signing in.</p>
      <button class="primary" :disabled="busy" @click="signIn">Sign in</button>
      <p v-if="error" class="error">{{ error }} <a href="/login">Get a new link</a></p>
    </template>

    <template v-else-if="sent">
      <div v-if="onRoster" class="note green">
        <p><b>Check your school e-mail.</b> Your address is on the class list, so a login link is on its way. The link works for 15 minutes.</p>
        <p class="small">Okul e-postanı kontrol et. Adresin sınıf listesinde, giriş bağlantısı gönderildi. Bağlantı 15 dakika geçerlidir. Gelen kutusunda yoksa "Gereksiz/Spam" klasörüne bak.</p>
      </div>
      <div v-else class="note red">
        <p><b>This address is not on the class list.</b> No login link was sent. Check the spelling of your school e-mail, or ask your instructor to add you to the list.</p>
        <p class="small">Bu e-posta adresi sınıf listesinde bulunamadı, giriş bağlantısı gönderilmedi. Adresin doğru yazıldığından emin ol veya hocandan listeye eklenmeni iste.</p>
      </div>
      <button class="link" @click="sent = false">Use another address</button>
    </template>

    <form v-else @submit.prevent="requestLink">
      <p class="muted">Write your school e-mail address. We send you a link to sign in. There is no password.</p>
      <label for="email">School e-mail <span class="hint">/ Okul e-postası</span></label>
      <input id="email" v-model="email" type="email" required autocomplete="email" placeholder="name@university.edu.tr" />
      <p><button class="primary" :disabled="busy">Send me the link</button></p>
      <p v-if="error" class="error">{{ error }}</p>
    </form>
  </div>
</template>
