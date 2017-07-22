### TODO清单
> 一些考虑到的问题，但在程序中未得到解决的

#### 一些问题
* ByteBufPool 申请回收实现
* Crypt加密解密接受ByteBuffer参数，其中ByteBuffer包含要加解密的字节，不包含其他内容，否则解包打包这些需要Crypt逻辑去做，将来包结构调整了之后，改动比较麻烦。
* 修改Connection，不使用ConnectionId，使用Ip:port
* DataQueue, ServerDataQueue connectionId应该为对应Connection提供，不应该从Bundle解析
* 从SocketChannel读写逻辑应该能通用
* 加密可以填充噪音数据
* 传输，协议，加解密，分开，协议解析部分与字节码读写分离, header，body，，压缩这部分针对body
* DataQueue put邏輯修改，不需要阻塞，通道写入这部分需要修改
* 读写效率太低，考虑使用线程池进行，包括解密，压缩之类的，使用Future机制，不适用CryptRunner对所有合适的SelectionKey进行遍历
* 读写可以批量进行
* 分流，有些本地应用不需要代理
* SOCKS代理
*
*
*
* Send Receive分为两种类型，先读header size，而后构建header， 读取完成header，根据数据构建body，另一种是没有header信息，先构建一个长度
  而后写入数据，最后根据数据信息，构建header
