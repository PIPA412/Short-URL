#!/bin/bash
# ============================================================
# Docker 构建与启动脚本
# 用法：
#   ./docker-build.sh build      — 构建镜像
#   ./docker-build.sh up         — 启动所有服务
#   ./docker-build.sh down       — 停止所有服务
#   ./docker-build.sh restart    — 重启服务
#   ./docker-build.sh logs       — 查看日志
#   ./docker-build.sh clean      — 停止服务并清除数据卷
# ============================================================

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
COMPOSE_FILE="${PROJECT_ROOT}/docker-compose.yml"

# 颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查 docker-compose 是否可用
check_prerequisites() {
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装，请先安装 Docker Desktop"
        exit 1
    fi
    if ! docker compose version &> /dev/null; then
        error "Docker Compose v2 不可用，请升级 Docker Desktop"
        exit 1
    fi
}

build_image() {
    info "开始构建后端镜像..."
    cd "${PROJECT_ROOT}/zsc_backend"
    docker build -t zsc-server:latest .
    info "镜像构建完成: zsc-server:latest"
    cd "${PROJECT_ROOT}"
}

start_services() {
    info "启动所有服务..."
    cd "${PROJECT_ROOT}"
    docker compose up -d
    info "服务启动状态："
    docker compose ps
    info ""
    info "服务启动可能需要 30-60 秒，查看日志："
    info "  ./docker-build.sh logs"
}

stop_services() {
    info "停止所有服务..."
    cd "${PROJECT_ROOT}"
    docker compose down
    info "服务已停止"
}

restart_services() {
    info "重新启动服务..."
    stop_services
    start_services
}

show_logs() {
    info "查看日志（按 Ctrl+C 退出）..."
    cd "${PROJECT_ROOT}"
    docker compose logs -f
}

clean_services() {
    warn "即将停止服务并清除所有数据卷（MySQL、Redis、日志数据将丢失）"
    read -p "确认清除？[y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        info "停止服务并清除数据卷..."
        cd "${PROJECT_ROOT}"
        docker compose down -v
        info "数据已清除"
    else
        info "操作已取消"
    fi
}

# -------- 主入口 --------
check_prerequisites

case "${1:-help}" in
    build)
        build_image
        ;;
    up)
        start_services
        ;;
    down)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    logs)
        show_logs
        ;;
    clean)
        clean_services
        ;;
    *)
        echo "用法: $0 {build|up|down|restart|logs|clean}"
        echo ""
        echo "   build     构建 Docker 镜像"
        echo "   up        启动所有服务 (docker compose up -d)"
        echo "   down      停止所有服务 (docker compose down)"
        echo "   restart   重启服务"
        echo "   logs      查看日志"
        echo "   clean     停止服务并清除数据"
        exit 1
        ;;
esac
