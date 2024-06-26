## FTP客户端的设计与实现 


采用Socket编程建立TCP连接，实现相关的操作类与服务器进行交互。

本次设计旨在解决如何通过解析TCP数据流中FTP服务器的响应数据包来实现与服务器的交互的问题。

设计思路主要包括建立与FTP服务器的连接、解析FTP服务器的响应数据包以及实现FTP客户端的基本功能。

解决问题的方案包括使用Socket编程建立与FTP服务器的连接，通过解析TCP数据流中的数据包来提取FTP服务器的响应数据，包括状态码等，然后根据响应数据进行相应的处理。

### 功能
                        命令 [参数]   命令解释    
          ------------------------------------------------------
                          help/info   获取命令帮助信息 
                             ls/dir   列出当前目录的文件和子目录 
                                pwd   打印当前工作目录的路径
                     cd [directory]   更改当前工作目录 
                   get [remotefile]   从服务器下载文件到本地 
                    put [localfile]   将本地文件上传到服务器 
               del [file/directory]   删除服务器上的文件或目录 
                    mkd [directory]   创建新的目录 
            rem [oldfile] [newfile]   重命名或移动文件 
                          quit/exit   关闭与FTP服务器的连接 
          ------------------------------------------------------

### 设计
- 用于接收和处理FTP服务器响应数据包的FTPResponseParser类和FTPResponse类。
- 提供FTP客户端功能实现的FTPClinet类。
- 获取用户的操作指令和参数，并调用相应的功能模块来完成操作，实现命令行交互的FTPMain类。


