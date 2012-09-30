{
   "_id": "_design/Demo",
   "_rev": "1-0123456789abcdef012345678",
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

