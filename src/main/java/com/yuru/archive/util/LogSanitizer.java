package com.yuru.archive.util;

/*
 * 2026-11-03 追加（doc/11-03checkCode.md 参照）
 * 利用者の入力値をログへ出力する際に、改行・制御文字を除去して長さを制限する。
 * 改行を含む入力で偽のログ行を作られる「ログインジェクション」を防ぐ。
 */
public final class LogSanitizer {

    private static final int MAX_LENGTH = 100;

    private LogSanitizer() {
    }

    public static String clean(String value) {
        if (value == null) {
            return null;
        }
        String cleaned = value.replaceAll("[\\p{Cntrl}]", "_");
        return cleaned.length() > MAX_LENGTH ? cleaned.substring(0, MAX_LENGTH) + "..." : cleaned;
    }
}
