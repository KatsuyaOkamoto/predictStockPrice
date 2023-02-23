# predictStockPrice
株価を予想し利確、損切りを記録するシステム
#### このシステムは私用で作られたものです。従ってスクレイピングするためのURLやパスワードをソースから削除しています。ご了承ください。
__________________________________________________________________________________________________________________________________________
### 機能
株を仮想的に投資をして実験するシステムです。Commandクラスから操作します。CUI操作です。<br>
1.手法の名前を入力するとデータベースに登録されます。 <br>
2.手法を選び投資する銘柄コードを入力すると現在の株価が出力されるので、利確値と損切り値を入力すると翌日の始値で仮想的に100株投資します。 <br>
3.未明にクローリングが行われ、利確および損切りが発生したらプロフィールを更新します。(クローリングの設定はcronやAutomatorで行う) <br>

現在はクローリングができる辺りまで完成しています。結果が出たらメールで出力したりCSV出力を行うシステムを作っていきます。
さらに株の情報をピックアップできるようなシステムも作成したいと思います。<br>
pingファイルにて「Download」を押すとフルスクリーンでご覧になることができます。
