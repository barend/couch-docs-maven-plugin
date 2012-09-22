{
   "_id": "_design/Demo",
   "views": {
       "sample_view": {
           "map": "function(doc) { if(doc.include === 'true') {emit(doc._rev, null)} }"
       }
   },
   "lists": {
   },
   "shows": {
   },
   "language": "javascript",
   "filters": {
   },
   "updates": {
   }
}

