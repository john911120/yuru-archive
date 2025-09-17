<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { StorageService } from '@/services/StorageService'
import type { Memo } from '@/types/Memo'

const storage = new StorageService('myMemo')
const memos = ref<Memo[]>([])

// Instance Method called
const load = () => {
  memos.value = storage.getItems()
}

onMounted(() => {
  const data = storage.getItems()
  console.log('getItems length:', data.length, data)
  memos.value = data
})

// Memo Data Delete
const remove = (id: number) => {
  if (!window.confirm('メーモの内容を削除しますか。')) return
  // Data Delete
  storage.delItem(id)
  window.alert('削除しました。')
  load()
}
</script>

<template>
  <div class="memo-list">
    <div class="item" v-for="m in memos" :key="m.id">
      <div class="d-flex pt-3">
        <div class="pb-3 mb-0 w-100">
          <div class="d-flex justify-content-between align-items-center">
            <b>{{ m.title }}</b>
            <div class="d-flex gap-1">
              <router-link
                :to="{ name: 'memo-form', params: { id: m.id } }"
                class="btn btn-outline-secondary"
                >編集</router-link
              >
              <span role="button" @click.prevent="remove(m.id)" class="btn btn-outline-secondary"
                >削除</span
              >
            </div>
          </div>
          <div class="mt-2">{{ m.content }}</div>
        </div>
      </div>
    </div>

    <router-link to="/memos/add" class="add btn btn-light w-100 mt-3">+ 追加する</router-link>
	<!-- 🔽 ゆるアーカイブに戻る ボタン -->
	<a href="/question/list" class="btn btn-outline-secondary w-100 mt-3">
	  ゆるアーカイブに戻る
	</a>
  </div>
</template>

<style scoped lang="scss">
.memo-list {
  .item {
    background: #fff;
    color: #212529;
    border: 1px solid #eee;
    display: block;
    padding: 20px 30px;
    margin: 15px 0;
  }
}
.add {
  font-size: 1.2rem;
  border: 2px solid #ccc;
  border-radius: 8px;
  transition: backgroud-color 0.3s ease;
}
.add:hover {
  background-color: skyblue;
}
</style>
