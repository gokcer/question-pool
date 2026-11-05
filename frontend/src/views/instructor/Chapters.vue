<script setup>
import { ref, onMounted } from 'vue'
import { get, put } from '../../api'

const chapters = ref([])
const saved = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    const list = await get('/chapters')
    chapters.value = await Promise.all(list.map((c) => get(`/chapters/${c.id}`)))
  } catch (e) {
    error.value = e.message
  }
})

async function readFile(event, chapter, field) {
  const file = event.target.files[0]
  if (file) chapter[field] = await file.text()
}

async function save(c) {
  error.value = ''
  try {
    await put(`/instructor/chapters/${c.id}`, { title: c.title, doc_en: c.doc_en, doc_tr: c.doc_tr, open: c.open })
    saved.value = c.id
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <h1>Chapter texts</h1>
  <p class="muted small">
    Students read these texts before they write questions, and the AI uses the English text for its checks and
    explanations. The format is Markdown. Do not put lab answers here.
  </p>
  <p v-if="error" class="error">{{ error }}</p>
  <details v-for="c in chapters" :key="c.id" class="card">
    <summary>
      <b>{{ c.title }}</b>
      <span class="small muted"> · EN {{ c.doc_en.length }} characters · TR {{ c.doc_tr.length }} characters · {{ c.open ? 'open' : 'closed' }}</span>
    </summary>
    <label>Title</label>
    <input v-model="c.title" type="text" />
    <label class="inline" style="margin-top: 10px"><input v-model="c.open" type="checkbox" /> Students can send new questions</label>
    <label>English text <input type="file" accept=".md,.txt,text/markdown,text/plain" @change="readFile($event, c, 'doc_en')" /></label>
    <textarea v-model="c.doc_en" rows="8"></textarea>
    <label>Turkish text <input type="file" accept=".md,.txt,text/markdown,text/plain" @change="readFile($event, c, 'doc_tr')" /></label>
    <textarea v-model="c.doc_tr" rows="8"></textarea>
    <p class="row">
      <button class="primary" @click="save(c)">Save</button>
      <span v-if="saved === c.id" class="small muted">Saved.</span>
    </p>
  </details>
</template>
