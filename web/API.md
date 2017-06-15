RESTFUL API INTERFACE
=====================

## 文献保存接口

将JSON格式的文献内容，POST到给接口，进行保存

* URL

  /api/task/add_new

* Method

  `POST`
  
*  URL Params
   无
   
* Data Params
   把JSON格式的内容作为body，POST到服务器端的该地址下。格式如下：
   
```json
{
  "title": "文献名称",
  "authors": ["作者1", "作者2"],
  "abstract": "简介",
  "journal": "期刊名称",
  "year": "2017",
  "volume": "01",
  "number": "01",
  "pages": "10-18",
  "doi": ""
}
```


