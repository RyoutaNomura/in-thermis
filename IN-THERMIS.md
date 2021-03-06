# IN-THERMIS

## 概要
各種形式・保存方法のドキュメントについて、
統一的なインタフェースで一括検索できる手段を提供する。  
それによって、蓄積してきた情報の活用を可能にする。

## 目的
社内では、様々なファイル形式、様々な格納方法でドキュメントを作成している。  
一応体系だったディレクトリ構成やブランチ構成で管理されており、
格納場所を把握しているものについては一応アクセスする手段はあった。  

ただ、検索手段は貧弱で、格納場所を把握していないものについては探しようがなく、
結局作成したドキュメントは一定期間を経ると参照もメンテナンスもされなくなっている。  
そのため同じようなドキュメントが乱立したり、不確実な情報が散在したりしている。  

そういった事態を防ぐために、各種ドキュメントをインデクシングし、
統一的なインタフェースでの一括検索を可能にする。  

検索が可能になることで、過去から蓄積してきたドキュメントを参照可能にし、
類似の不要なドキュメントの乱立、情報不足を防ぐことができるようになる。

## メリット
##### A. マルチ・フォーマット対応
各種ドキュメントはtxt, xls, ods, pdfなど様々なフォーマットで保存されている。  
そういった様々な形式のファイルをフォーマットの違いを意識することなく、検索することができる。

##### B. マルチ・ロケーション対応
各種ドキュメントは、ファイルサーバやsvn、Gitなどのソースコード管理システム、wikiなどに分散して格納されている。  
複数の場所に格納されているドキュメントを場所を意識せずに一括して検索することができる。

##### C. 視認性の高い検索結果表示
検索条件にヒットした箇所を中心としたプレビューを一覧することができる。  
該当箇所はハイライトされており、個別のドキュメントを開くことなく目的のドキュメントを素早く探すことができる。  

##### D. 柔軟な検索条件
基本的には１つの検索条件入力欄に単語を入力することによって、素早く検索を行うことができる。  
格納場所や形式、更新日時など複雑な検索条件が必要な場合は、別途検索条件を指定することもできる。  
また、検索した結果から特定の条件で絞込をかけることもできる。

##### E. 検索結果からのファイルアクセスが容易
検索した結果から該当のファイルのURIを取得できる。  
ブラウザ経由でダウンロードすることもできる。  
目的のファイルに素早くアクセスすることができる。

##### F. 関連情報の参照
ファイル内に登場する人名の一覧や、機能の一覧など、ドキュメント内の情報の集計結果を確認することができる。  
ドキュメントと外部の情報を有機的に結びつけることで、ドキュメントの活用が期待できる。  

##### G. 柔軟なインデックス作成機能  
複数のURIをインデクシングの対象とすることができます。  
特定のファイルのみ対象にしたり、特定のファイルのみを除外したりするために、ファイル名のパターンマッチに基づいた対象選定が可能です。  
また、インデクシングの負荷を集中させないように、URIごとに実行時間を指定したりすることができます。

##### H. 高速な検索
検索のレスポンスは高速で、操作する人を待たせることがない。  
また、スケールする構造のため、ユーザーが増えた際にも速度劣化を防ぐ対応を取りやすい。  

## 機能一覧

### 概要

|   | メリット                             | 機能No. | 機能                                         | 優先度 | 実装済 |
|---|--------------------------------------|---------|----------------------------------------------|--------|--------|
| A | マルチフォーマット対応               | 1       | 各種ファイル形式に対応するインデクサ         | A      | ○     |
|   |                                      | 2       | 未対応ファイルをテキストとしてインデクシング | C      | ✕      |
|   |                                      | 3       | 文字コードの自動認識                         | A      | ○     |
| B | マルチロケーション対応               | 1       | 各種格納方式に対応したインデクサ             | A      | △     |
| C | 視認性の高い検索結果表示             | 1       | 検索該当箇所のプレビュー                     | A      | ○     |
|   |                                      | 2       | 該当箇所のハイライト                         | A      | ○     |
|   |                                      | 3       | 検索結果の並び順指定                         | B      | ○     |
|   |                                      | 4       | ファイル種別によるアイコン表示               | B      | ○     |
| D | 柔軟な検索条件                       | 1       | 単語指定による検索                           | A      | ○     |
|   |                                      | 2       | 複合単語指定による検索                       | B      | ○     |
|   |                                      | 3       | 日本語、英語による検索                       | A      | ✕      |
|   |                                      | 4       | 検索辞書の拡張                               | C      | ✕      |
|   |                                      | 5       | シームレスな操作                             | B      | ○     |
|   |                                      | 6       | フィルタリング                               | B      | ○     |
| E | 検索結果からのファイルアクセスが容易 | 1       | URIの参照                                    | B      | ○     |
|   |                                      | 2       | ファイルダウンロード                         | C      | ✕      |
| F | 関連情報の参照                       | 1       | 関連人名の確認                               | C      | ✕      |
|   |                                      | 2       | 関連情報の定義                               | C      | ✕      |
| G | 柔軟なインデックス作成機能           | 1       | 定期実行                                     | B      | ✕      |
|   |                                      | 2       | インデックス作成動作の指定                   | C      | ✕      |
|   |                                      | 3       | パターンマッチによる対象指定・除外指定       | C      | ✕      |
| H | 高速な検索                           | 1       | 高速なレスポンス                             | A      | △     |
|   |                                      | 2       | 分散可能環境                                 | A      | ○     |
| I | 簡単な設定機能                       | 1       | 各種設定                                     | A      | ✕      |

### 詳細

##### A.1 各種ファイル形式に対応するインデクサ
以下のファイルフォーマットに対応する。  

 | フォーマット              | 拡張子      | 対応状況 |
 | ------------------------- | ----------- | -------- |
 | テキスト                  | .txt, .md   | ○       |
 | Microsoft Word            | .doc, .docx | ○       |
 | Microsoft Excel           | .xls, .xlsx | ○       |
 | Microsoft PowerPoint      | .ppt, .pptx | ○       |
 | OpenDocument SpreadSheet  | .ods        | ○       |
 | OpenDocument Text         | .odt        | ○       |
 | OpenDocument Presentation | .odp        | ○       |
 | PDF                       | .pdf        | ○       |
 | HTML                      | .html, .htm | ○       |
 | Rich Text                 | .rtf        | ○       |

##### A.2 未対応ファイルをテキストとしてインデクシング
上記フォーマットと認識できない場合に、テキストファイルと解釈してインデクシングする。

##### A.3 文字コードの自動認識
テキストファイルとして認識した場合には、文字コードを自動で認識してインデクシングする。  
システム設定で優先順位は指定可能。

##### B.1 各種格納方式に対応したインデクサ
以下の格納方式に対応する  

| 格納場所          | 対応状況 |
| ----------------- | -------- |
| ローカルファイル  | ○       |
| CIFS/Samba        | ○       |
| NFS               | ✕        |
| Subversion        | ✕        |
| Git               | ✕        |
| Redmine           | ✕        |
| MediaWiki         | ○       |

##### C.1 検索該当箇所のプレビュー
検索結果の一覧内で、検索該当箇所の前後を含めたプレビューを確認することができる。  

##### C.2 該当箇所のハイライト
検索したワードについては、該当箇所がハイライトされている  

##### C.3 検索結果の並び順指定
検索ワードが含まれる数が多い順、更新日時が新しい順などが指定できる。

##### C.4 ファイル種別によるアイコン表示
ファイル種別に基づいたアイコンが表示され、何のファイルなのか視認性高く確認できる。  

##### D.1 単語指定による検索
ワンボックスの検索条件入力欄に単語を入力するだけで検索が可能。  
ファイル名、格納形式、日時等は指定不要。

##### D.2 複合単語指定による検索
スペースで区切って複数単語を入力することで、複数の単語にマッチするドキュメントを検索することができる。  
検索用語自体も形態素解析して、検索を実施する。

##### D.3 日本語、英語による検索
日本語も英語も同列にインデクシングし、同様に検索ができる。

##### D.4 検索辞書の拡張
辞書に該当がなかった用語や、用語分割を行った単語はあとでまとめて確認することができる。  
ドメイン特化の用語についても辞書に含めてインデックスの制度を高めることができる。  

##### D.5 シームレスな操作
次へボタンなどのテンポを悪くする操作は排除し、スクロールするだけで追加分の読み込みを可能にする。

##### D.6 フィルタリング
ロケーション、ファイル種別、名称などでのフィルタリングが可能


##### E.1 URIの参照
格納方式に依存せず、ドキュメントに到達するためのURIを参照することができる。  
コピーも可能。

##### E.2 ファイルダウンロード
ブラウザから目的のドキュメントをダウンロードすることができる。  
認証がかかっている場所のファイルダウンロードに際しては、
ユーザーによる認証を求めることにより、セキュリティレベルを下げずに直接ダウンロードが可能。

##### F.1 関連人名の確認
ドキュメントに含まれる人名を確認することができる。  
より詳細な情報を誰に確認したらよいかの端緒となる。  

##### F.2 関連情報の定義
紐付ける情報を独自に定義することができる。  

##### G.1 定期実行
インデクサごとに実行時間を定めることができる。  
ファイルは０時から、svnは2時からなど、リソースの混雑状況を加味したスケジューリングが可能  

##### G.2 インデックス作成方法の指定
差分作成、洗い替えなど、挙動を選ぶことができる。  

##### G.3 インデックス作成対象のパターンマッチ  
ファイルパターンを指定して、特定のファイルのみインデクシングしたり、除外したりすることができる。

##### H.1 高速なレスポンス
100ms程度でレスポンスを返却し、検索を待たせない。

##### H.2 分散可能環境
RDBを使用せず、サーバー増強によるスケールを可能にする。

##### H.1. 各種設定
リソースの場所やインデクサの適用順序、認証情報などを簡単に設定できる。
設定すればSCMにかぎらず利用ができる

