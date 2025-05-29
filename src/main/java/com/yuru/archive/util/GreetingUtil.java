package com.yuru.archive.util;

import java.time.LocalTime;

//📌 時間帯に応じた挨拶メッセージを返却するユーティリティクラスです。

public class GreetingUtil {
    /**
     * 🕒 現在時刻に基づき、以下の区分で挨拶メッセージを返します。
     * - 午前（〜11時）: 「おはようございます。」
     * - 午後（12時〜17時）: 「こんにちは！」
     * - 夜間（18時〜）: 「こんばんは！」
     *
     * @return 挨拶メッセージ（日本語）
     */
	public static String getGreetingMessage() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "おはようございます。";
        else if (hour < 18) return "こんにちは！";
        else return "こんばんは！";
    }
}
