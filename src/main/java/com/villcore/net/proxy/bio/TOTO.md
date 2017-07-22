### BIO

#### 
* Connection 包装两个线程，每个线程执行， 读取 -> 输出功能， encrypt decrypt
* Header部分加密长度固定，加解密密文
* 总size， 定长随机字符串加密，加密的定长真实header + 加密body， + 混淆数据