<script setup>
import { ref, onMounted } from 'vue'
import { get, post, when } from '../../api'

const teachers = ref([])
const email = ref('')
const fullName = ref('')
const result = ref('')
const error = ref('')

async function load() {
  teachers.value = await get('/instructor/teachers')
}
onMounted(() => load().catch((e) => (error.value = e.message)))

async function addTeacher() {
  error.value = ''
  result.value = ''
  try {
    await post('/instructor/teachers', { email: email.value, full_name: fullName.value })
    result.value = `${fullName.value.trim()} was added.`
    email.value = ''
    fullName.value = ''
    await load()
  } catch (e) {
    error.value = e.message
  }
}
</script>

<template>
  <h1>Teachers <span class="muted small">/ Öğretmenler</span></h1>
  <p class="muted small">
    These accounts can open the instructor screens. A new teacher signs in the same way as everyone else: they write
    their e-mail on the login page and receive a one-time link.
  </p>
  <p v-if="error" class="error">{{ error }}</p>

  <div class="scroll card">
    <table>
      <thead>
        <tr>
          <th>Name</th><th>E-mail</th><th>Nickname</th><th>Status</th><th>Last login</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="t in teachers" :key="t.id">
          <td>{{ t.full_name }}</td>
          <td>{{ t.email }}</td>
          <td>{{ t.nickname }}</td>
          <td>
            <span class="badge" :class="t.active ? 'green' : 'amber'">{{ t.active ? 'active' : 'inactive' }}</span>
          </td>
          <td>{{ t.last_login_at ? when(t.last_login_at) : 'never' }}</td>
        </tr>
      </tbody>
    </table>
    <p v-if="!teachers.length" class="muted">No teachers yet.</p>
  </div>

  <h2>Add a teacher <span class="muted small">/ Öğretmen ekle</span></h2>
  <form @submit.prevent="addTeacher">
    <label for="t-email">School e-mail <span class="hint">/ Okul e-postası</span></label>
    <input id="t-email" v-model="email" type="email" required placeholder="name@university.edu.tr" />
    <label for="t-name">Full name <span class="hint">/ Ad soyad</span></label>
    <input id="t-name" v-model="fullName" type="text" required placeholder="Ali Veli" />
    <p>
      <button class="primary" :disabled="!email.trim() || !fullName.trim()">Add teacher</button>
    </p>
  </form>
  <p v-if="result" class="note green">{{ result }}</p>
</template>
