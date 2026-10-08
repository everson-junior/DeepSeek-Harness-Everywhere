package com.deepseek.everywhere.model;

/**
 * Listener para receber notificações de alteração de estado do DeepSeek Harness.
 */
public interface HarnessStatusListener {
    void onStatusChanged(HarnessStatus status, String url, Integer port, String error);
}
