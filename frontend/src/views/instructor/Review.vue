<script setup>
import { ref, reactive, onMounted } from 'vue'
import { get, post, when } from '../../api'
import AiReview from '../../components/AiReview.vue'

const statuses = [
  ['PENDING', 'Waiting'],
  ['NEEDS_REVISION', 'Sent back'],
  ['APPROVED', 'Approved'],
  ['REJECTED', 'Rejected'],
]
const letters = ['A', 'B', 'C', 'D']
const status = ref('PENDING')
const chapterId = ref('')
const chapters = ref([])
const counts = ref({})
const questions = ref([])
const notes = reactive({})
const error = ref('')

async function load() {
  error.value = ''
  try {
    const query = `?status=${status.value}` + (chapterId.value ? `&chapter_id=${chapterId.value}` : '')
    const [list, c] = await Promise.all([get('/instructor/questions' + query), get('/instructor/questions/counts')])
    questions.value = list
    counts.value = Object.fromEntries(c.map((x) => [x.status, x.count]))
  } catch (e) {
    error.value = e.message
  }
}
onMounted(async () => {
  chapters.value = await get('/chapters').catch(() => [])
  load()
})

async function decide(q, action) {
  error.value = ''
  try {
    await post(`/instructor/questions/${q.id}/decision`, { action, note: notes[q.id] || '' })
    questions.value = questions.value.filter((x) => x.id !== q.id)
    counts.value = Object.fromEntries((await get('/instructor/questions/counts')).map((x) => [x.status, x.count]))
  } catch (e) {
    error.value = `Question #${q.id}: ${e.message}`
  }
}
async function rerun(q, what) {
  await post(`/instructor/questions/${q.id}/${what}`)
  setTimeout(load, 6000)
}
function pick(s) {
  status.value = s
  load()
}
</script>

<template>
  <h1>Review questions</h1>
  <div class="row spread">
    <div class="tabs" style="margin: 0">
      <button v-for="[s, label] in statuses" :key="s" :class="{ on: status === s }" @click="pick(s)">{{ label }} ({{ counts[s] || 0 }})</button>
    </div>
    <select v-model="chapterId" style="width: auto" @change="load">
      <option value="">All chapters</option>
      <option v-for="c in chapters" :key="c.id" :value="c.id">{{ c.title }}</option>
    </select>
  </div>
  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="!questions.length" class="muted">Nothing here.</p>

  <div v-for="q in questions" :key="q.id" class="card">
    <div class="row small muted">
      <span>#{{ q.id }} · Week {{ q.chapter_number }}</span>
      <RouterLink :to="`/instructor/students/${q.author_id}`">{{ q.author_name }}</RouterLink>
      <span>({{ q.author }})</span>
      <span>{{ when(q.created_at) }}</span>
      <span v-if="q.attempts">{{ q.correct_attempts }}/{{ q.attempts }} answered correctly</span>
      <span v-if="q.open_flags" class="badge red">{{ q.open_flags }} open flags</span>
    </div>
    <p class="pre"><b>{{ q.stem }}</b></p>
    <div v-for="l in letters" :key="l" class="option" :class="{ correct: q.correct === l }">
      <span class="letter">{{ l }}</span>
      <div class="grow">
        <div>{{ q['option_' + l.toLowerCase()] }}</div>
        <div class="why">{{ q['rationale_' + l.toLowerCase()] }}</div>
      </div>
    </div>
    <p class="small muted">Reference: {{ q.reference }}</p>
    <AiReview :status="q.ai_status" :review="q.ai_review" />
    <p v-if="q.ai_status === 'SKIPPED'" class="small muted">No automatic check (the AI key is not set).</p>
    <div v-if="q.explanation" class="note small"><b>AI explanation shown to students:</b><div class="pre">{{ q.explanation }}</div></div>
    <div v-if="q.instructor_note" class="small muted">Your last note: {{ q.instructor_note }}</div>

    <textarea v-model="notes[q.id]" rows="2" placeholder="Note for the student (needed for 'Send back' and 'Reject')" style="margin-top: 8px"></textarea>
    <div class="row" style="margin-top: 8px">
      <button v-if="q.status !== 'APPROVED'" class="primary" @click="decide(q, 'APPROVE')">Approve</button>
      <button v-if="q.status !== 'NEEDS_REVISION'" @click="decide(q, 'REVISE')">Send back</button>
      <button v-if="q.status !== 'REJECTED'" class="danger" @click="decide(q, 'REJECT')">Reject</button>
      <RouterLink :to="`/questions/${q.id}/edit`" class="btn">Edit text</RouterLink>
      <RouterLink :to="`/questions/${q.id}`" class="btn">Open</RouterLink>
      <button class="link" @click="rerun(q, 'ai-check')">Run check again</button>
      <button v-if="q.status === 'APPROVED'" class="link" @click="rerun(q, 'explain')">Write explanation again</button>
    </div>
  </div>
</template>
