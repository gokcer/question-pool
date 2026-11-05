<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { get, post, when } from '../../api'

const router = useRouter()
const exams = ref([])
const chapters = ref([])
const form = reactive({ title: 'SENG 403 Midterm 1', forms: 2, skip_flagged: true, counts: {} })
const error = ref('')
const busy = ref(false)
const total = computed(() => Object.values(form.counts).reduce((sum, n) => sum + (Number(n) || 0), 0))

onMounted(async () => {
  try {
    ;[exams.value, chapters.value] = await Promise.all([get('/instructor/exams'), get('/chapters')])
    for (const c of chapters.value) form.counts[c.id] = 0
  } catch (e) {
    error.value = e.message
  }
})

async function create() {
  busy.value = true
  error.value = ''
  try {
    const { id } = await post('/instructor/exams', form)
    router.push(`/instructor/exams/${id}`)
  } catch (e) {
    error.value = e.message
    busy.value = false
  }
}
</script>

<template>
  <h1>Exams</h1>
  <p class="muted small">
    An exam is a random draw from the approved questions. Every form (A, B, …) has the same questions; the order of
    the questions inside a chapter and the order of the options are different in each form.
  </p>

  <RouterLink v-for="e in exams" :key="e.id" :to="`/instructor/exams/${e.id}`" class="card">
    <b>{{ e.title }}</b>
    <div class="small muted">{{ e.questions }} questions · {{ e.forms }} forms · made {{ when(e.created_at) }}</div>
  </RouterLink>

  <h2>Make a new exam</h2>
  <form class="card" @submit.prevent="create">
    <label for="title">Title</label>
    <input id="title" v-model="form.title" type="text" maxlength="120" required />

    <label>Questions from each chapter</label>
    <table>
      <thead><tr><th>Chapter</th><th>Approved in pool</th><th style="width: 110px">Take</th></tr></thead>
      <tbody>
        <tr v-for="c in chapters" :key="c.id">
          <td>{{ c.title }}</td>
          <td>{{ c.approved }}</td>
          <td><input v-model.number="form.counts[c.id]" type="number" min="0" :max="c.approved" style="width: 90px; padding: 6px" /></td>
        </tr>
        <tr><td><b>Total</b></td><td></td><td><b>{{ total }}</b></td></tr>
      </tbody>
    </table>

    <label for="forms">Number of forms</label>
    <select id="forms" v-model.number="form.forms" style="width: auto">
      <option :value="1">1 (A)</option><option :value="2">2 (A, B)</option><option :value="3">3 (A, B, C)</option><option :value="4">4 (A, B, C, D)</option>
    </select>
    <p><label class="inline"><input v-model="form.skip_flagged" type="checkbox" /> Do not use questions that have an open problem report</label></p>
    <p v-if="error" class="error">{{ error }}</p>
    <button class="primary" :disabled="busy || !total">Draw the questions</button>
  </form>
</template>
