package com.atguigu.exam.context;

public class BaseContext {
    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    // 设置当前登录用户ID
    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    // 获取当前登录用户ID
    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    // 清理 (防止内存泄漏)
    public static void removeCurrentId() {
        THREAD_LOCAL.remove();
    }
}
