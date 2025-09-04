import type { Memo, StorageData, NewMemo } from '@/types/Memo'

export class StorageService {
  //Storage Name [private field ESNext]
  #storageName: string

  //Constructor with type annotation
  constructor(storageName: string) {
    // if not in StorageName
    if (!storageName) {
      throw new Error('Please Enter the Storage Name')
    }
    this.#storageName = storageName
  }

  // Storage Data Read
  #getStorageData(): StorageData {
    // ストレージの名前をキーとして利用し、データを照会する。
    const json = localStorage.getItem(this.#storageName)
    // JSONデータがあったら、パーシングしてデータをリターンする
    if (json) {
      return JSON.parse(json)
    }

    return {
      items: [],
      nextId: 1
    }
  }
  // Storage Data Saving
  #saveStorageData(obj: unknown) {
    if (typeof obj !== 'object' || obj === null || Array.isArray(obj)) {
      throw new Error('Memo data must be a non-null object')
    }

    // JSON Character Translated
    const json = JSON.stringify(obj)
    localStorage.setItem(this.#storageName, json)
  }

  // insert the new Object
  addItem(item: NewMemo) { 
    const storageData = this.#getStorageData()

    // 現在のnextIdを項目のidに適応し、1増加させる。
    const memo: Memo = {
      ...item,
      id: storageData.nextId++,
      title: item.title,
      content: item.content,
      createdAt: new Date().toISOString()
    }

    storageData.items.push(memo)
    this.#saveStorageData(storageData)
  }

  // 全体の項目を照会する
  getItems(): Memo[] {
    return this.#getStorageData().items
  }

  // 特定の項目を照会する
  getItem(id: number) {
    return this.#getStorageData().items.find((i) => i.id === id)
  }

  // 特定の項目を修正する
  setItem(item: Memo) {
    const storageData = this.#getStorageData()

    for (let i = 0; i < storageData.items.length; i += 1) {
      if (storageData.items[i].id === item.id) {
        storageData.items[i] = item
        break
      }
    }
    this.#saveStorageData(storageData)
  }

  // 特定の項目を削除する
  delItem(id: number) {
    const storageData = this.#getStorageData()
    const idx = storageData.items.findIndex((i) => i.id === id)

    if (idx > -1) {
      storageData.items.splice(idx, 1)
      this.#saveStorageData(storageData)
    }
  }
}
