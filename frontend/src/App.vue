<script setup>
import { useUserStore } from './stores/user'

const userStore = useUserStore()
</script>

<template>
  <div id="app">
    <el-container v-if="userStore.token">
      <el-header class="app-header">
        <div class="header-left">
          <el-link href="/" :underline="false" class="logo">短链接系统</el-link>
        </div>
        <div class="header-right">
          <span class="username">{{ userStore.username }}</span>
          <el-button type="danger" text @click="userStore.logout">退出</el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
    <router-view v-else />
  </div>
</template>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: 'Helvetica Neue', Helvetica, Arial, 'PingFang SC', 'Microsoft YaHei', sans-serif; }
#app { min-height: 100vh; background: #f5f7fa; }
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #409eff;
  padding: 0 24px;
}
.header-left .logo {
  color: #fff;
  font-size: 20px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
}
.username { font-size: 14px; }
</style>
