export type ThemeMode = 'light' | 'dark' | 'auto'

const STORAGE_KEY = 'yuru-theme'

function resolveTheme (mode: ThemeMode): 'light' | 'dark' {
    if(mode === 'auto') {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    // モードが ライト | ダーク の場合は、そのままリターン
    return mode
}

export function useTheme() {
    const getMode = (): ThemeMode => 
    (document.documentElement.dataset.userThemeMode as ThemeMode) || 'auto'

    const getAppliedTheme = (): 'light' | 'dark' => 
    (document.documentElement.getAttribute('data-bs-theme') as 'light' | 'dark') || resolveTheme(getMode())

    const setMode = (mode: ThemeMode) => {
        const theme = resolveTheme(mode)
        document.documentElement.setAttribute('data-bs-theme', theme)
        document.documentElement.dataset.userThemeMode = mode
        localStorage.setItem(STORAGE_KEY, mode)
    }

    // 最初の1回だけ、セーフされた好みを反映する「無ければ、自動的にAUTO」
    const init = () => {
        const saved = (localStorage.getItem(STORAGE_KEY) as ThemeMode) || 'auto'
        setMode(saved)
    }

    //システムのダークモード変更も追跡する「モードがAutoの場合のみに反映する」
    const listenSystem = () => {
        const mq = window.matchMedia('(prefers-color-scheme: dark)')
        const handler = () => {
            if(getMode() === 'auto') setMode('auto')
        }
        mq.addEventListener?.('change', handler)
        return () => mq.removeEventListener?.('change', handler)
    }

    return { getMode, setMode, listenSystem, getAppliedTheme, init }
}