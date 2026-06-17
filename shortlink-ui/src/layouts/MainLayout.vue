<template>
  <el-container class="main-layout">
    <!-- Sidebar -->
    <Sidebar :collapsed="appStore.sidebarCollapsed" />

    <el-container>
      <!-- Header -->
      <Header @toggle-sidebar="appStore.toggleSidebar()" />

      <!-- Main Content -->
      <el-main>
        <router-view />
      </el-main>

      <!-- Footer -->
      <Footer />
    </el-container>
  </el-container>
</template>

<script setup>
import { useAppStore } from '@/store/app'
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import Footer from './Footer.vue'

const appStore = useAppStore()
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.el-main {
  background-color: #f0f2f5;
  min-height: 0;
  padding: 20px;
}
</style>
