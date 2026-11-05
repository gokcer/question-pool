<script setup>
import { session, isInstructor } from './store'
import { post } from './api'

async function logout() {
  await post('/auth/logout').catch(() => {})
  session.user = null
  location.assign('/login')
}
</script>

<template>
  <header v-if="session.user && !$route.meta.print" class="topbar">
    <RouterLink to="/" class="brand">SENG 403 <span>Question Pool</span></RouterLink>
    <nav>
      <RouterLink to="/">Chapters</RouterLink>
      <RouterLink v-if="!isInstructor()" to="/mine">My questions</RouterLink>
      <template v-if="isInstructor()">
        <RouterLink to="/instructor/review">Review</RouterLink>
        <RouterLink to="/instructor/students">Students</RouterLink>
        <RouterLink to="/instructor/flags">Flags</RouterLink>
        <RouterLink to="/instructor/exams">Exams</RouterLink>
        <RouterLink to="/instructor/chapters">Chapter texts</RouterLink>
      </template>
    </nav>
    <div class="who">
      <span title="Other students see this name, not your real name">{{ session.user.nickname }}</span>
      <button class="link" @click="logout">Sign out</button>
    </div>
  </header>
  <main :class="{ print: $route.meta.print }">
    <RouterView :key="$route.path" />
  </main>
</template>
