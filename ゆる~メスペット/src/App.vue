<script setup lang="ts"></script>

<template>
  <!-- 変数基盤のユーチルを適用する -->
  <div class="app bg-body text-body min-vh-100">
    <div class="container py-4">
      <!-- Header -->
      <header class="pb-4 mb-4 border-bottom d-flex align-items-center">
        <!-- text-dark → リンク用の変数ユーチル-->
        <router-link to="/" class="logo-link link-body-emphasis text-decoration-none">
          <b class="fs-4">ゆる〜メスペット</b>
        </router-link>
        <div class="ms-auto">
          <ThemeSwitcher />
        </div>
      </header>
      <!-- main -->
      <main>
        <!-- router view-->
        <RouterView></RouterView>
      </main>
      <!-- footer-->
      <footer class="pt-4 mt-4 border-top text-body-secondary">
        &copy; 2025, JohnDev ゆる〜メスペット. All rights reserved.
      </footer>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.app .container {
  // 横の長さと大きさを制限
  max-width: 576px;
}
.logo-link {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}
.logo-link:hover {
  background-color: skyblue;
  text-decoration: underline;
}
</style>
