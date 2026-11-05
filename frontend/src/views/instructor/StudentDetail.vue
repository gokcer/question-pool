<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { get, when } from '../../api'
import StatusBadge from '../../components/StatusBadge.vue'

const id = useRoute().params.id
const s = ref(null)
const error = ref('')
onMounted(async () => {
  try {
    s.value = await get(`/instructor/students/${id}`)
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <p class="small"><RouterLink to="/instructor/students">← Students</RouterLink></p>
  <p v-if="error" class="error">{{ error }}</p>
  <template v-if="s">
    <h1>{{ s.full_name }}</h1>
    <p class="muted">{{ s.student_no }} · {{ s.email }} · nickname {{ s.nickname }} · last login {{ when(s.last_login_at) }}</p>

    <h2>Questions written ({{ s.questions.length }})</h2>
    <RouterLink v-for="q in s.questions" :key="q.id" :to="`/questions/${q.id}`" class="card">
      <div class="row small muted"><span>#{{ q.id }} · Week {{ q.chapter_number }}</span><StatusBadge :status="q.status" /><span>{{ when(q.created_at) }}</span></div>
      <div>{{ q.stem }}</div>
    </RouterLink>

    <h2>Answers by chapter</h2>
    <p v-if="!s.attempts_by_chapter.length" class="muted">No answers yet.</p>
    <table v-else>
      <thead><tr><th>Week</th><th>Answered</th><th>Correct</th></tr></thead>
      <tbody><tr v-for="a in s.attempts_by_chapter" :key="a.chapter_number"><td>{{ a.chapter_number }}</td><td>{{ a.attempts }}</td><td>{{ a.correct }}</td></tr></tbody>
    </table>

    <h2>Comments ({{ s.comments.length }})</h2>
    <div v-for="c in s.comments" :key="c.id" class="card">
      <div class="small muted">
        {{ when(c.created_at) }} on <RouterLink :to="`/questions/${c.question_id}`">#{{ c.question_id }}</RouterLink>: {{ c.stem }}
        <span v-if="c.deleted" class="badge red">hidden</span>
      </div>
      <div class="pre">{{ c.body }}</div>
    </div>

    <h2>Problems reported ({{ s.flags.length }})</h2>
    <div v-for="f in s.flags" :key="f.id" class="card">
      <div class="small muted">{{ when(f.created_at) }} on <RouterLink :to="`/questions/${f.question_id}`">#{{ f.question_id }}</RouterLink></div>
      <div class="pre">{{ f.reason }}</div>
    </div>
  </template>
</template>
