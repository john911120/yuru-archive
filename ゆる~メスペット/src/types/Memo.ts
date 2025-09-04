export interface Memo {
  /*
     id : 固有識別子「ストレージの内部で、作られる。」
     content : メーモの内容
     createdAt : ISO文字列[生成日付け] 
    */
  id: number
  title: string
  content: string
  createdAt: string
}

export interface StorageData {
  items: Memo[]
  nextId: number
}
export type NewMemo = Omit<Memo, 'id' | 'createdAt'> 
