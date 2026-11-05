<script setup>
import { ref, onMounted } from 'vue'
import { get, post, when } from '../../api'

const students = ref([])
const csv = ref('')
const result = ref(null)
const error = ref('')

async function load() {
  students.value = await get('/instructor/students')
}
onMounted(() => load().catch((e) => (error.value = e.message)))

async function importRoster() {
  error.value = ''
  try {
    result.value = await post('/instructor/roster', { csv: csv.value })
    csv.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <h1>Students and participation</h1>
  <p v-if="error" class="error">{{ error }}</p>
  <div class="scroll card">
    <table>
      <thead>
        <tr>
          <th>Student</th><th>No</th><th>Nickname</th><th title="Questions written / approved">Questions</th>
          <th title="Chapters with at least one question">Chapters</th><th title="Answered / correct">Answers</th>
          <th>Comments</th><th>Flags</th><th>Last login</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in students" :key="s.id">
          <td><RouterLink :to="`/instructor/students/${s.id}`">{{ s.full_name }}</RouterLink></td>
          <td>{{ s.student_no }}</td>
          <td>{{ s.nickname }}</td>
          <td>{{ s.questions }} / {{ s.approved }}</td>
          <td>{{ s.chapters }}</td>
          <td>{{ s.attempts }} / {{ s.correct }}</td>
          <td>{{ s.comments }}</td>
          <td>{{ s.flags }}</td>
          <td>{{ s.last_login_at ? when(s.last_login_at) : 'never' }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!students.length" class="muted">No students yet. Add the class list below.</p>
  </div>

  <h2>Add or update the class list</h2>
  <p class="small muted">
    One student per line: <code>school e-mail, full name, student number</code>. Comma, semicolon or tab between the
    parts. A student who is already on the list keeps the same nickname. Only these addresses can sign in.
  </p>
  <textarea v-model="csv" rows="6" placeholder="ali.veli@university.edu.tr, Ali Veli, 20210001"></textarea>
  <p><button class="primary" :disabled="!csv.trim()" @click="importRoster">Import</button></p>
  <div v-if="result" class="note">
    Added {{ result.added }}, updated {{ result.updated }}.
    <div v-if="result.skipped.length">Skipped lines (wrong format): <div v-for="line in result.skipped" :key="line" class="small pre">{{ line }}</div></div>
  </div>
</template>
