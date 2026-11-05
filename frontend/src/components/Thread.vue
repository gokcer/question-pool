<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post, when } from '../api'
import { isInstructor } from '../store'

const props = defineProps({ questionId: [String, Number] })
const comments = ref([])
const body = ref('')
const replyTo = ref(null)
const busy = ref('')
const error = ref('')

async function load() {
  comments.value = await get(`/questions/${props.questionId}/comments`)
}
onMounted(() => load().catch((e) => (error.value = e.message)))

// Two levels on screen: a comment and its replies. A reply to a reply is shown under the same first comment.
const tree = computed(() => {
  const byId = Object.fromEntries(comments.value.map((c) => [c.id, c]))
  const rootOf = (c) => {
    while (c.parent_id && byId[c.parent_id]) c = byId[c.parent_id]
    return c
  }
  const roots = comments.value.filter((c) => !c.parent_id || !byId[c.parent_id])
  return roots.map((root) => ({ root, replies: comments.value.filter((c) => c !== root && rootOf(c) === root) }))
})

async function send(askAi) {
  if (!body.value.trim()) return
  busy.value = askAi ? 'ai' : 'send'
  error.value = ''
  try {
    await post(`/questions/${props.questionId}/${askAi ? 'ask-ai' : 'comments'}`, { body: body.value, parent_id: replyTo.value?.id ?? null })
    body.value = ''
    replyTo.value = null
    await load()
  } catch (e) {
    error.value = e.message
  }
  busy.value = ''
}

async function hide(c) {
  if (!confirm('Hide this comment from students?')) return
  await post(`/instructor/comments/${c.id}/hide`)
  await load()
}
</script>

<template>
  <h2>Discussion</h2>
  <p class="small muted">
    Ask what you did not understand, or explain how you thought. You can write in Turkish.
    Be kind: everybody is learning.
  </p>
  <p v-if="!comments.length" class="muted">No comments yet. Be the first.</p>

  <div v-for="t in tree" :key="t.root.id">
    <div v-for="c in [t.root, ...t.replies]" :key="c.id" :class="['comment', { reply: c !== t.root, ai: c.is_ai }]">
      <div class="row small muted">
        <b>{{ c.author }}</b>
        <span v-if="c.real_name && !c.is_ai">({{ c.real_name }})</span>
        <span v-if="c.wrote_question" class="badge blue">wrote this question</span>
        <span v-if="c.mine" class="badge">you</span>
        <span>{{ when(c.created_at) }}</span>
        <button class="link" @click="replyTo = c">Reply</button>
        <button v-if="isInstructor()" class="link" @click="hide(c)">Hide</button>
      </div>
      <div class="pre">{{ c.body }}</div>
    </div>
  </div>

  <div class="card">
    <div v-if="replyTo" class="small muted">
      Reply to {{ replyTo.author }} <button class="link" @click="replyTo = null">cancel</button>
    </div>
    <textarea v-model="body" maxlength="2000" placeholder="Write a comment or a question…"></textarea>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="row" style="margin-top: 8px">
      <button class="primary" :disabled="!!busy || !body.trim()" @click="send(false)">Send</button>
      <button :disabled="!!busy || !body.trim()" @click="send(true)">{{ busy === 'ai' ? 'The AI helper is thinking…' : 'Send and ask the AI helper' }}</button>
    </div>
    <p class="small muted">The AI helper answers from the chapter text. It can make mistakes; ask the instructor when you are not sure.</p>
  </div>
</template>
