<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { get, post, del } from '../../api'

const id = useRoute().params.id
const router = useRouter()
const letters = ['A', 'B', 'C', 'D']
const exam = ref(null)
const error = ref('')

async function load() {
  exam.value = await get(`/instructor/exams/${id}`)
}
onMounted(() => load().catch((e) => (error.value = e.message)))

async function act(item, what) {
  error.value = ''
  if (what === 'remove' && !confirm('Take this question out of the exam?')) return
  try {
    await post(`/instructor/exams/${id}/items/${item.item_id}/${what}`)
    await load()
  } catch (e) {
    error.value = e.message
  }
}
async function removeExam() {
  if (!confirm('Delete this exam? The questions stay in the pool.')) return
  await del(`/instructor/exams/${id}`)
  router.push('/instructor/exams')
}
</script>

<template>
  <p class="small"><RouterLink to="/instructor/exams">← Exams</RouterLink></p>
  <p v-if="error" class="error">{{ error }}</p>
  <template v-if="exam">
    <h1>{{ exam.title }}</h1>
    <p class="muted">{{ exam.items.length }} questions · {{ exam.forms }} forms</p>

    <div class="card">
      <b>Print</b>
      <span class="small muted"> (opens a print page; choose "Save as PDF" in the print window)</span>
      <div class="row" style="margin-top: 8px">
        <template v-for="n in exam.forms" :key="n">
          <RouterLink :to="`/instructor/exams/${id}/print/${n}`" target="_blank" class="btn">Form {{ letters[n - 1] }}</RouterLink>
          <RouterLink :to="`/instructor/exams/${id}/print/${n}?key=1`" target="_blank" class="btn">Key {{ letters[n - 1] }}</RouterLink>
        </template>
      </div>
      <p class="small note amber">If you swap, remove or edit a question after printing, print every form and key again: the order changes.</p>
    </div>

    <h2>Questions</h2>
    <p class="small muted">Options are shown in the order the student wrote them. The printed forms shuffle them.</p>
    <div v-for="(q, i) in exam.items" :key="q.item_id" class="card">
      <div class="row small muted">
        <span>{{ i + 1 }} · Week {{ q.chapter_number }} · #{{ q.question_id }} · {{ q.author_name }}</span>
        <span v-if="q.attempts">{{ q.correct_attempts }}/{{ q.attempts }} answered correctly</span>
        <span v-if="q.open_flags" class="badge red">{{ q.open_flags }} open reports</span>
        <span v-if="q.status !== 'APPROVED'" class="badge red">not approved any more</span>
      </div>
      <p class="pre"><b>{{ q.stem }}</b></p>
      <div v-for="l in letters" :key="l" class="small" :style="q.correct === l ? 'font-weight: 700; color: var(--green)' : ''">
        {{ l }}) {{ q['option_' + l.toLowerCase()] }}
      </div>
      <div class="row" style="margin-top: 8px">
        <button @click="act(q, 'swap')">Swap for another</button>
        <button class="danger" @click="act(q, 'remove')">Remove</button>
        <RouterLink :to="`/questions/${q.question_id}/edit`" class="btn">Edit text</RouterLink>
        <RouterLink :to="`/questions/${q.question_id}`" class="btn">Open</RouterLink>
      </div>
    </div>
    <p><button class="danger" @click="removeExam">Delete this exam</button></p>
  </template>
</template>
