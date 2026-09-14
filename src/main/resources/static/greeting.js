document.addEventListener('DOMContentLoaded', () => {
    const greetingElement = document.getElementById('greetingMessage');

    if (!greetingElement) {
        return;
    }

    // 接続したユーザーのブラウザー現地時刻を基準に挨拶を切り替える。
    const hour = new Date().getHours();

    let greeting;

    if (hour < 12) {
        greeting = 'おはようございます。';
    } else if (hour < 18) {
        greeting = 'こんにちは！';
    } else {
        greeting = 'こんばんは！';
    }

    greetingElement.textContent = greeting;
});
