import{a as g,c as h,A as f}from"./generated-flow-imports-11cf5c2f.js";import{a as l,c as o,v as p,e as R}from"./network_utils-5322e3e6.js";import"./indexhtml-b0aa4eaa.js";if(typeof PDFJSDev<"u"&&PDFJSDev.test("MOZCENTRAL"))throw new Error('Module "./fetch_stream.js" shall not be used with MOZCENTRAL builds.');function d(a,e,t){return{method:"GET",headers:a,signal:t==null?void 0:t.signal,mode:"cors",credentials:e?"include":"same-origin",redirect:"follow"}}function u(a){const e=new Headers;for(const t in a){const r=a[t];typeof r>"u"||e.append(t,r)}return e}class y{constructor(e){this.source=e,this.isHttp=/^https?:/i.test(e.url),this.httpHeaders=this.isHttp&&e.httpHeaders||{},this._fullRequestReader=null,this._rangeRequestReaders=[]}get _progressiveDataLength(){var e,t;return(e=(t=this._fullRequestReader)===null||t===void 0?void 0:t._loaded)!==null&&e!==void 0?e:0}getFullReader(){return g(!this._fullRequestReader,"PDFFetchStream.getFullReader can only be called once."),this._fullRequestReader=new b(this),this._fullRequestReader}getRangeReader(e,t){if(t<=this._progressiveDataLength)return null;const r=new m(this,e,t);return this._rangeRequestReaders.push(r),r}cancelAllRequests(e){this._fullRequestReader&&this._fullRequestReader.cancel(e);for(const t of this._rangeRequestReaders.slice(0))t.cancel(e)}}class b{constructor(e){this._stream=e,this._reader=null,this._loaded=0,this._filename=null;const t=e.source;this._withCredentials=t.withCredentials||!1,this._contentLength=t.length,this._headersCapability=h(),this._disableRange=t.disableRange||!1,this._rangeChunkSize=t.rangeChunkSize,!this._rangeChunkSize&&!this._disableRange&&(this._disableRange=!0),typeof AbortController<"u"&&(this._abortController=new AbortController),this._isStreamingSupported=!t.disableStream,this._isRangeSupported=!t.disableRange,this._headers=u(this._stream.httpHeaders);const r=t.url;fetch(r,d(this._headers,this._withCredentials,this._abortController)).then(i=>{if(!l(i.status))throw o(i.status,r);this._reader=i.body.getReader(),this._headersCapability.resolve();const n=c=>i.headers.get(c),{allowRangeRequests:s,suggestedLength:_}=p({getResponseHeader:n,isHttp:this._stream.isHttp,rangeChunkSize:this._rangeChunkSize,disableRange:this._disableRange});this._isRangeSupported=s,this._contentLength=_||this._contentLength,this._filename=R(n),!this._isStreamingSupported&&this._isRangeSupported&&this.cancel(new f("Streaming is disabled."))}).catch(this._headersCapability.reject),this.onProgress=null}get headersReady(){return this._headersCapability.promise}get filename(){return this._filename}get contentLength(){return this._contentLength}get isRangeSupported(){return this._isRangeSupported}get isStreamingSupported(){return this._isStreamingSupported}async read(){await this._headersCapability.promise;const{value:e,done:t}=await this._reader.read();return t?{value:e,done:t}:(this._loaded+=e.byteLength,this.onProgress&&this.onProgress({loaded:this._loaded,total:this._contentLength}),{value:new Uint8Array(e).buffer,done:!1})}cancel(e){this._reader&&this._reader.cancel(e),this._abortController&&this._abortController.abort()}}class m{constructor(e,t,r){this._stream=e,this._reader=null,this._loaded=0;const i=e.source;this._withCredentials=i.withCredentials||!1,this._readCapability=h(),this._isStreamingSupported=!i.disableStream,typeof AbortController<"u"&&(this._abortController=new AbortController),this._headers=u(this._stream.httpHeaders),this._headers.append("Range",`bytes=${t}-${r-1}`);const n=i.url;fetch(n,d(this._headers,this._withCredentials,this._abortController)).then(s=>{if(!l(s.status))throw o(s.status,n);this._readCapability.resolve(),this._reader=s.body.getReader()}).catch(s=>{if((s==null?void 0:s.name)!=="AbortError")throw s}),this.onProgress=null}get isStreamingSupported(){return this._isStreamingSupported}async read(){await this._readCapability.promise;const{value:e,done:t}=await this._reader.read();return t?{value:e,done:t}:(this._loaded+=e.byteLength,this.onProgress&&this.onProgress({loaded:this._loaded}),{value:new Uint8Array(e).buffer,done:!1})}cancel(e){this._reader&&this._reader.cancel(e),this._abortController&&this._abortController.abort()}}export{y as PDFFetchStream};
