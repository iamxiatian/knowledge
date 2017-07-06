# knowledge
Mining Scientific Publications

# 编译运行

本机安装JDK和sbt，然后运行：
```
sbt stage
cd target/universe/stage
bin/knowledge
```

数据集： http://abc.com/1.zip


## 文献导入

CNKI提供了数据导出功能，可以把文献的基本信息以XML形式导出，为了方便分析，
我们把导出的数据重新导入到MONGODB数据库中。假设所有导出的XML文件都存在于
./data/papers目录或者其子目录下，并以".xml"结尾，则执行：

```sbtshell
bin/knowledge --dumpIn -d="./data/papers"
```

即可实现文件导入，导入的文献默认位于MongoDB的knowledge数据库中的publication
集合中。

## 论文亮点的统计

```bash
sbt console
printSectionStatsMessage
printCoverage("results", "abstract", "discussion", "methods", "background", "conclusion", "introduction", "research model and hypotheses")
```

以上第一条命令进入Scala控制台，第二条命令统计论文亮点数据集中的章节类型及
文档频度，第三条命令则输出亮点在指定类型章节中的出现频度