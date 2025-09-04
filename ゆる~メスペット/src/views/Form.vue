<script setup lang="ts">
import { reactive, watch } from 'vue'
import { StorageService } from '@/services/StorageService'
import { useRouter, useRoute } from 'vue-router'
import type { Memo, NewMemo } from '@/types/Memo'
import { onMounted } from 'vue'

// Create Storage Service Instance
const storage = new StorageService('myMemo')

// Router Object
const router = useRouter()
const route = useRoute()

// 反応形状態
const state = reactive({
  memo: {
    title: '',
    content: ''
  } as any
})

// Data Submit
const submit = async () => {
  // route Object Parameter ID value Existed case
  if ('id' in state.memo) {
    storage.setItem(state.memo as Memo)
  } else {
    // Insert Memo
    storage.addItem(state.memo as Omit<Memo, 'id'>)
  }
  // after save return the list
  await router.replace({ name: 'memo-list' })

  // Print the Notice Message
  window.alert('登録又は修正しました。')

  // Move to the main Page
  router.push({ path: '/memos' })
}

function loadFromRoute() {
  const idParam = route.params.id
  if (!idParam) {
    state.memo = { title: '', content: '' }
    return
  }
  const id = Number(idParam)
  if (!Number.isNaN(id)) {
    const loaded = storage.getItem(id)
    if (loaded) state.memo = { ...loaded }
  }
}

onMounted(loadFromRoute)
watch(() => route.params.id, loadFromRoute)
</script>

<template>
  <form class="detail" @submit.prevent="submit">
    <div class="mb-3">
      <label for="title" class="form-label">タイトル</label>
      <!-- title print -->
      <input type="text" id="title" class="form-control p-3" v-model="state.memo.title" />
    </div>
    <div class="mb-3">
      <label for="content" class="form-label">内容</label>
      <!-- input content -->
      <textarea id="content" class="form-control p-3" v-model="state.memo.content"></textarea>
    </div>
    <button type="submit" class="btn btn-primary w-100 py-3">💾セーフする</button>
  </form>
</template>
