import{a as R,i as F,M as E,U as b,s as q}from"./generated-flow-imports-11cf5c2f.js";function w(i){let c=!0,t=l("filename\\*","i").exec(i);if(t){t=t[1];let e=g(t);return e=unescape(e),e=x(e),e=m(e),d(e)}if(t=h(i),t){const e=m(t);return d(e)}if(t=l("filename","i").exec(i),t){t=t[1];let e=g(t);return e=m(e),d(e)}function l(e,n){return new RegExp("(?:^|;)\\s*"+e+'\\s*=\\s*([^";\\s][^;\\s]*|"(?:[^"\\\\]|\\\\"?)+"?)',n)}function s(e,n){if(e){if(!/^[\x00-\xFF]+$/.test(n))return n;try{const r=new TextDecoder(e,{fatal:!0}),a=q(n);n=r.decode(a),c=!1}catch{if(/^utf-?8$/i.test(e))try{n=decodeURIComponent(escape(n)),c=!1}catch{}}}return n}function d(e){return c&&/[\x80-\xff]/.test(e)&&(e=s("utf-8",e),c&&(e=s("iso-8859-1",e))),e}function h(e){const n=[];let r;const a=l("filename\\*((?!0\\d)\\d+)(\\*?)","ig");for(;(r=a.exec(e))!==null;){let[,f,p,u]=r;if(f=parseInt(f,10),f in n){if(f===0)break;continue}n[f]=[p,u]}const o=[];for(let f=0;f<n.length&&f in n;++f){let[p,u]=n[f];u=g(u),p&&(u=unescape(u),f===0&&(u=x(u))),o.push(u)}return o.join("")}function g(e){if(e.startsWith('"')){const n=e.slice(1).split('\\"');for(let r=0;r<n.length;++r){const a=n[r].indexOf('"');a!==-1&&(n[r]=n[r].slice(0,a),n.length=r+1),n[r]=n[r].replace(/\\(.)/g,"$1")}e=n.join('"')}return e}function x(e){const n=e.indexOf("'");if(n===-1)return e;const r=e.slice(0,n),o=e.slice(n+1).replace(/^[^']*'/,"");return s(r,o)}function m(e){return!e.startsWith("=?")||/[\x00-\x19\x80-\xff]/.test(e)?e:e.replace(/=\?([\w-]*)\?([QqBb])\?((?:[^?]|\?(?!=))*)\?=/g,function(n,r,a,o){if(a==="q"||a==="Q")return o=o.replace(/_/g," "),o=o.replace(/=([0-9a-fA-F]{2})/g,function(f,p){return String.fromCharCode(parseInt(p,16))}),s(r,o);try{o=atob(o)}catch{}return s(r,o)})}return""}function C({getResponseHeader:i,isHttp:c,rangeChunkSize:t,disableRange:l}){R(t>0,"Range chunk size must be larger than zero");const s={allowRangeRequests:!1,suggestedLength:void 0},d=parseInt(i("Content-Length"),10);return!Number.isInteger(d)||(s.suggestedLength=d,d<=2*t)||l||!c||i("Accept-Ranges")!=="bytes"||(i("Content-Encoding")||"identity")!=="identity"||(s.allowRangeRequests=!0),s}function I(i){const c=i("Content-Disposition");if(c){let t=w(c);if(t.includes("%"))try{t=decodeURIComponent(t)}catch{}if(F(t))return t}return null}function D(i,c){return i===404||i===0&&c.startsWith("file:")?new E('Missing PDF "'+c+'".'):new b(`Unexpected server response (${i}) while retrieving PDF "${c}".`,i)}function P(i){return i===200||i===206}export{P as a,D as c,I as e,C as v};
