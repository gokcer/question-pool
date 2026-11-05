<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { get } from '../api'
import { session, isInstructor } from '../store'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const chapter = ref(null)
const questions = ref([])
const error = ref('')
const tab = ref(route.query.tab === 'questions' ? 'questions' : 'read')
const lang = ref(localStorage.getItem('docLang') || 'en')
const filter = ref('all')
const target = session.user.questions_per_chapter

onMounted(async () => {
  try {
    ;[chapter.value, questions.value] = await Promise.all([get(`/chapters/${id}`), get(`/chapters/${id}/questions`)])
  } catch (e) {
    error.value = e.message
  }
})

function setTab(t) {
  tab.value = t
  router.replace({ query: { tab: t } })
}
function setLang(l) {
  lang.value = l
  localStorage.setItem('docLang', l)
}

// Headings get the same ids as on GitHub, so "Contents" links inside a chapter text work.
const slug = (text) => text.trim().toLowerCase().replace(/[^\p{L}\p{N}\s_-]/gu, '').replace(/\s/g, '-')
const docHtml = computed(() => {
  const text = lang.value === 'tr' ? chapter.value?.doc_tr : chapter.value?.doc_en
  if (!text) return ''
  const box = document.createElement('div')
  box.innerHTML = DOMPurify.sanitize(marked.parse(text))
  box.querySelectorAll('h1, h2, h3, h4').forEach((h) => (h.id = slug(h.textContent)))
  return box.innerHTML
})
const mineCount = computed(() => questions.value.filter((q) => q.mine && q.status !== 'REJECTED').length)
const canWrite = computed(() => isInstructor() || (chapter.value?.open && mineCount.value < target))
const shown = computed(() =>
  questions.value.filter((q) => {
    if (filter.value === 'mine') return q.mine
    if (filter.value === 'todo') return q.status === 'APPROVED' && !q.mine && !q.attempted
    return true
  }),
)
// Students open their own questions and approved ones. The instructor opens everything.
const canOpen = (q) => q.mine || q.status === 'APPROVED' || isInstructor()
</script>

<template>
  <p class="small"><RouterLink to="/">← Chapters</RouterLink></p>
  <p v-if="error" class="error">{{ error }}</p>
  <template v-if="chapter">
    <h1>{{ chapter.title }}</h1>
    <div class="tabs">
      <button :class="{ on: tab === 'read' }" @click="setTab('read')">1. Read the chapter</button>
      <button :class="{ on: tab === 'questions' }" @click="setTab('questions')">2. Questions ({{ questions.length }})</button>
    </div>

    <section v-if="tab === 'read'">
      <div class="row">
        <span class="muted small">Language:</span>
        <label class="inline"><input type="radio" :checked="lang === 'en'" @change="setLang('en')" /> English</label>
        <label class="inline"><input type="radio" :checked="lang === 'tr'" @change="setLang('tr')" /> Türkçe</label>
      </div>
      <div v-if="docHtml" class="card doc" v-html="docHtml"></div>
      <p v-else class="muted">The chapter text is not ready in this language yet.</p>
      <p><button class="primary" @click="setTab('questions')">Go to the questions →</button></p>
    </section>

    <section v-else>
      <div class="row spread">
        <div class="tabs" style="margin: 0">
          <button :class="{ on: filter === 'all' }" @click="filter = 'all'">All</button>
          <button v-if="!isInstructor()" :class="{ on: filter === 'todo' }" @click="filter = 'todo'">Not answered</button>
          <button v-if="!isInstructor()" :class="{ on: filter === 'mine' }" @click="filter = 'mine'">Mine</button>
        </div>
        <RouterLink v-if="canWrite" :to="`/chapters/${id}/new`" class="btn primary">+ Write a question</RouterLink>
      </div>
      <p v-if="!isInstructor()" class="small muted">
        You wrote {{ mineCount }} of {{ target }} questions for this chapter.
        <span v-if="!chapter.open">This chapter is closed for new questions.</span>
        Read the list before you write, so you do not ask the same thing again.
      </p>
      <p v-if="!shown.length" class="muted">No questions here yet.</p>

      <component :is="canOpen(q) ? 'RouterLink' : 'div'" v-for="q in shown" :key="q.id" :to="`/questions/${q.id}`" class="card">
        <div>{{ q.stem }}</div>
        <div class="row small muted" style="margin-top: 6px">
          <span>#{{ q.id }}</span>
          <span v-if="q.mine" class="badge blue">Mine</span>
          <span v-else>by {{ q.author }}</span>
          <span v-if="q.status === 'PENDING'" class="badge amber">Waiting for approval</span>
          <span v-else-if="q.status === 'NEEDS_REVISION'" class="badge amber">Needs changes</span>
          <span v-else-if="q.status === 'REJECTED'" class="badge red">Rejected</span>
          <template v-else-if="!q.mine && !isInstructor()">
            <span v-if="!q.attempted" class="badge">Not answered</span>
            <span v-else-if="q.my_correct" class="badge green">✓ Correct</span>
            <span v-else class="badge red">✗ Wrong</span>
          </template>
          <span v-if="q.comments">💬 {{ q.comments }}</span>
        </div>
      </component>
    </section>
  </template>
</template>
