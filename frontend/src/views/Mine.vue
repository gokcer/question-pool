<script setup>
import { ref, onMounted } from 'vue'
import { get } from '../api'
import StatusBadge from '../components/StatusBadge.vue'

const questions = ref(null)
const error = ref('')
onMounted(async () => {
  try {
    questions.value = await get('/questions/mine')
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <h1>My questions</h1>
  <p v-if="error" class="error">{{ error }}</p>
  <p v-if="questions && !questions.length" class="muted">
    You have no questions yet. Open a <RouterLink to="/">chapter</RouterLink>, read it, and write your first question.
  </p>
  <RouterLink v-for="q in questions" :key="q.id" :to="`/questions/${q.id}`" class="card">
    <div class="small muted">{{ q.chapter_title }}</div>
    <div>{{ q.stem }}</div>
    <div class="row small muted" style="margin-top: 6px">
      <StatusBadge :status="q.status" />
      <span v-if="q.ai_status === 'RUNNING'">Automatic check is running…</span>
      <span v-if="q.status === 'APPROVED'">{{ q.attempts }} answers</span>
      <span v-if="q.comments">💬 {{ q.comments }}</span>
    </div>
    <div v-if="q.instructor_note && q.status !== 'APPROVED'" class="note amber small">Instructor: {{ q.instructor_note }}</div>
  </RouterLink>
</template>
