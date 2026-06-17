<template>
  <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
    <div class="logo" @click="$router.push('/')" style="cursor: pointer;">
      <el-icon :size="24"><Link /></el-icon>
      <span v-show="!collapsed" class="logo-text">ShortLink</span>
    </div>

    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :collapse-transition="false"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>仪表盘</span>
      </el-menu-item>
      <el-menu-item index="/links">
        <el-icon><Link /></el-icon>
        <span>短链管理</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps({
  collapsed: { type: Boolean, default: false }
})

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  margin-left: 10px;
  font-size: 18px;
  font-weight: 600;
  white-space: nowrap;
}

.el-menu {
  border-right: none;
}
</style>
