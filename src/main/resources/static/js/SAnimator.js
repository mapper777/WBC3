var colors={cSV : {
  background: "white",
  border:"black",
  highlight: {
    background: "red"
  }
},
sSV : {
  background: "red",
  border:"black",
  highlight: {
    background: "red"
  }
},
mSV : {
  background: "magenta",
  border:"black",
  highlight: {
    background: "magenta"
  }
},
fFV : {
  background: "cyan",
  border:"black",
  highlight: {
    background: "cyan"
  }
}};
function tHistClear(){
  var nRow;
  var inp=document.getElementById("tInpStr");
  inp.selStart=0;
  inp.selEnd=0;
  var tScan=document.getElementById("tblScaner");
  if((nRow=inp.row)!==undefined){
    tScan.rows[nRow].cells[0].innerText="";
    tScan.rows[2].cells[parseInt(inp.state)+2].innerText="";
  }
  tScan.rows[2].cells[2].innerText="►";
  inp.state=0;
  inp.step=1;
  inp.oldCol=-1;
  var tbl=document.getElementById("tHist");
  var i;
  for(i=tHist.rows.length-1;i>0;i-=1)
    tbl.rows[1].remove();
}
function gHistClear(){
  var inp=document.getElementById("tInpStr");
  inp.selStart=0;
  inp.selEnd=0;
  var tScan=document.getElementById("grScanner");
//  if((nRow=inp.row)!==undefined){
//    tScan.rows[nRow].cells[0].innerText="";
//    tScan.rows[2].cells[parseInt(inp.state)+2].innerText="";
//  }
  tScan.rows[2].cells[0].innerText="►";
  inp.step=1;
  inp.oldCol=-1;
  var tbl=document.getElementById("tHist");
  var i;
  for(i=tHist.rows.length-1;i>0;i-=1)
    tbl.rows[1].remove();
  var nToC=[];
  if(inp.finSt!=undefined){
    nToC.push(_G.network.body.data.nodes._data[parseInt(inp.finSt)]);
    nToC[nToC.length-1].color=colors.fFV;
  }
  if(inp.state!=undefined){
    nToC.push(_G.network.body.data.nodes._data[parseInt(inp.state)]);
    nToC[nToC.length-1].color=colors.cSV;
  }
  nToC.push(_G.network.body.data.nodes._data[0]);
  nToC[nToC.length-1].color=colors.sSV;
  _G.network.body.data.nodes.update(nToC);
  inp.state=0;
  inp.finSt=0;
}
function tOneStep(){
  var lim;
  var ls=1;
  var inp=document.getElementById("tInpStr");
  var val=inp.value;
  if(inp.value==="")
    return;
  if(inp.selEnd===0)
    inp.selEnd=1;
  var tScan=document.getElementById("tblScaner");
  lim=tScan.rows.length;
  var symb,vSymb,ind=4;
  if(inp.selStart>=val.length){
    symb="EOF";
    vSymb=symb;
  }else{
    symb=val.substring(inp.selStart++,inp.selEnd++);
    vSymb=symb;
    if(symb===" ")
      vSymb="\d32"
    if((symb=="\\")&&(inp.selEnd<val.length)){
      var ads=val.substring(inp.selStart++,inp.selEnd++);
      if(ads=="t"){
        symb="\t";
        vSymb="\\t";
        ls=2;
      }else if(ads=="r"){
        symb="\r";
        vSymb="\\r";
        ls=2;
      }else if(ads=="n"){
        symb="\n";
        vSymb="\\n";
        ls=2;
      }else{
        symb=ads;
        vSymb=ads;
      }
    }
    for(ind=4;ind<lim;ind++){
      var mark=tScan.rows[ind].cells[1].getAttribute("rng");
      if(symb=="\""){
        if(tScan.rows[ind].cells[1].getAttribute("quote")=="1")
          break;
      }else if(symb=="\r"){
        if(tScan.rows[ind].cells[1].getAttribute("retc")=="1")
          break;        
      }else{
        if(mark.indexOf(symb)>=0)
          break;
      }
    }
  }
  var tHist=document.getElementById("tHist");
  var hRow;
  var step=inp.step;
  var to=-1;
  if(ind>=lim){
    var curSt=parseInt(inp.state)+2;
    var out=0;
    for(var ix=4;ix<lim;ix++){
      var ic=parseInt(tScan.rows[ix].cells[curSt].innerText)+2;
      if(ic!==curSt){
        if(out===0)
          out=ic;
        else
          if(out!==ic)
            break;
      }else
        if((to===-1)||(to===ic))
          to=ic;
        else
          to=lim;
    }
    if(to>0){
      ind=to;
      tScan.rows[inp.row].cells[0].innerText=" ";
    }
  }
  if(ind<lim){
    var oldCol=inp.oldState;
    if(!isNaN(oldCol)){
      tScan.rows[2].cells[oldCol+2].innerText=" ";
    }
    var oldRow=inp.row;
    if(!isNaN(oldRow)){
      tScan.rows[oldRow].cells[0].innerText=" ";
      tScan.rows[oldRow].cells[inp.oldState+2].classList.remove("blnk");
    }
    if(to<0)
      tScan.rows[ind].cells[0].innerText="►";
    if(step>=tHist.rows.length){
      hRow=tHist.insertRow(step);
      hRow.innerHTML="<td> <td> <td> <td> ";
    }else
      hRow=tHist.rows[step];
    hRow.cells[0].innerText=" "+step;
    hRow.cells[1].innerText=" "+inp.state;
    hRow.cells[2].innerText=" "+vSymb;
    inp.row=ind;
    var col=parseInt(inp.state);
    tScan.rows[ind].cells[col+2].classList.add("blnk");
    tScan.rows[2].cells[col+2].innerText="▼";
    inp.step=parseInt(step)+1;
    inp.oldCol=col;
    inp.oldState=col;
    var newState=tScan.rows[ind].cells[col+2].innerText.trim();
    if(isNaN(parseInt(newState))){
      if(++step>=tHist.rows.length){
        hRow=tHist.insertRow(step);
        hRow.innerHTML="<td> <td> <td> <td> ";
      }else
        hRow=tHis.rows[step];
      hRow.cells[0].innerText=" "+step;
      hRow.cells[3].innerText=" Error";
      inp.state=0;
    }else{
      if(parseInt(newState)>0)
        inp.state=newState;
      else{
        if(++step>=tHist.rows.length){
          hRow=tHist.insertRow(step);
          hRow.innerHTML="<td> <td> <td> <td> ";
        }else
          hRow=tHis.rows[step];
        hRow.cells[0].innerText=" "+step;
        hRow.cells[1].innerText=" "+newState;
        var finS=document.getElementById("finalS");
        for(var indF=2;indF<finS.rows.length;indF++){
          var rw=finS.rows[indF];
          if(parseInt(rw.cells[1].innerText)==newState){
            hRow.cells[3].innerText=rw.cells[3].innerText;
            break;
          }
        }
        if(tScan.rows[ind].cells[col+2].classList.toString().indexOf("fv")>=0){
          inp.selStart-=ls;
          inp.selEnd-=ls;
        }
        inp.state=0;
      }
    }
    inp.step=parseInt(step)+1;
  }else{
    hRow=tHist.insertRow(tHist.rows.length);
    hRow.innerHTML="<td> <td> <td> <td> ";
    hRow.cells[0].innerText=" "+step;
    hRow.cells[3].innerText=" Error";
    hRow.cells[2].innerText=vSymb;
    inp.state=0;
//    tHistClear();
  }
}
function tAllSteps(){
  if(document.getElementById("tInpStr").value.trim()==""){
    alert("Входная последовательность символов пуста, нечего делать.");
    return;
  }
//  _G.stepCnt=100;
  var tInt=parseInt(document.getElementById("tInterval").value);
  if(isNaN(tInt)||(tInt<100)||(tInt>10000))
    tInt=2000;
  document.getElementById("tInterval").value=tInt;
  tHistClear();
  _G.interval=setInterval(goTStep,tInt);
}
function goTStep(){
  var tH=document.getElementById("tHist");
  var l=tH.rows.length;
  var t=tH.rows[l-1].cells[1].innerText;
  var tt=parseInt(t);
  if((t=="Error")||(!isNaN(tt))&&(tt==-1))
    clearInterval(_G.interval);
  else
    tOneStep();
}
function textGraph(){
  var vtS=document.getElementById("vtScanner");
  var vgS=document.getElementById("vgScanner");
  var w=0,h=0;
  if(vtS.clientHeight>0){
    w=vtS.clientWidth;
    h=vtS.clientHeight;
  }
  w=(w<800)?800:w;
  h=(h<660)?660:h;
  vtS.classList.toggle("hid");
  vgS.classList.toggle("hid");
  vgS.style.width=w+"px";
  vgS.style.height=h+"px";
  vtS=document.getElementById("butTG");
  vtS.value=(vtS.value=="графа"?"текста":"графа");
}
function gOneStep(){
  var lim;
  var ls=1;
  var inp=document.getElementById("tInpStr");
  var oldSt=inp.state;
  var val=inp.value;
  if(inp.value==="")
    return;
  if(inp.selEnd===0)
    inp.selEnd=1;
  var tScan=document.getElementById("grScanner");
  lim=tScan.rows.length;
  var symb,vSymb,ind=parseInt(inp.state)+2;
  tScan.rows[ind].cells[0].innerText=" ";
  if(inp.selStart>=val.length){
    symb="EOF";
    vSymb=symb;
  }else{
    symb=val.substring(inp.selStart++,inp.selEnd++);
    vSymb=symb;
    if(symb===" ")
      vSymb="\\d32"
    if((symb=="\\")&&(inp.selEnd<val.length)){
      var ads=val.substring(inp.selStart++,inp.selEnd++);
      if(ads=="t"){
        symb="\t";
        vSymb="\\d09";
        ls=2;
      }else if(ads=="r"){
        symb="\r";
        vSymb="\\d10";
        ls=2;
      }else if(ads=="n"){
        symb="\n";
        vSymb="\\d13";
        ls=2;
      }else{
        symb=ads;
        vSymb=ads;
      }
    }
  }
  var links=tScan.rows[ind].cells[3].innerText;
  var newSt=0;
  if((ind==2)&&(symb=="EOF"))
    newSt=-1;
  else{
    var byOther=0;
    var lnk=links.split("->");
    var mark=lnk[0].trim();
    for(var il=1;il<lnk.length;il++){
      var nlink=lnk[il].trim();
      var nd=nlink.indexOf(" ");
      if(mark.substring(0,1)=="["){
        mark=mark.substring(1,mark.length-1);
        if(mark=="other"){
          byOther=parseInt(nlink);
          mark=nlink.substring(nd).trim();
          continue;
        }
      }
      if(vSymb.length>1){
        if(mark.indexOf(vSymb)>=0){
          newSt=parseInt(nlink);
          break;
        }
      }else{
        var im;
        while((im=mark.indexOf("\\d"))>=0){
          if(im==0)
            mark=mark.substring(im+4);
          else
            mark=mark.substring(0,im)+mark.substring(im+4);
        }
        if(mark.length>0){
          if(vSymb=="-"){
            if((mark.substring(0,1)=="-")||(mark.substring(mark.length-1)=="-")){
              newSt=parseInt(nlink);
              break;              
            }
          }else{
            var fnd=false;
            for(im=0;im<mark.length;im++){
              var curC=mark.substring(im,im+1);
              if(vSymb<curC)
                break;
              if(vSymb==curC){
                fnd=true;
                break;
              }
              if((im<mark.length-1)&&(mark.substring(im+1,im+2)=="-"))
                if((im<mark.length-2)&&(mark.substring(im+2,im+3)>=vSymb)){
                  fnd=true;
                  break;
                }
            }
            if(fnd){
              newSt=parseInt(nlink);
              break;              
            }
          }
        }
      }
      if(nd>0){
        mark=nlink.substring(nd).trim();
      }
    }
    if((newSt==0)&&(byOther!=0))
      newSt=byOther;
  }
  var tHist=document.getElementById("tHist");
  var step=parseInt(inp.step);
  if(newSt!=0){
    var hRow;
    if(step>=tHist.rows.length){
      hRow=tHist.insertRow(step);
      hRow.innerHTML="<td> <td> <td> <td> ";
    }else
      hRow=tHist.rows[step];
    hRow.cells[0].innerText=" "+step;
    hRow.cells[1].innerText=" "+inp.state;
    hRow.cells[2].innerText=" "+vSymb;
    var nToC=[];
    if(oldSt!=newSt){
      nToC.push(_G.network.body.data.nodes._data[oldSt]);
      nToC.push(_G.network.body.data.nodes._data[newSt]);
      if(inp.finSt!=0){
        nToC.push(_G.network.body.data.nodes._data[parseInt(inp.finSt)]);
        nToC[2].color=colors.fFV;
        inp.finSt=0;
      }
    }
    if(newSt>0){
      if(oldSt!=newSt){
        nToC[0].color=colors.cSV;
        nToC[1].color=colors.sSV;
      }
      inp.state=newSt;
      tScan.rows[newSt+2].cells[0].innerText="►";
    }else{
      if(oldSt!=newSt){
        nToC[0].color=colors.cSV;
        nToC[1].color=colors.mSV;
        nToC.push(_G.network.body.data.nodes._data[0]);
        nToC[nToC.length-1].color=colors.sSV;
      }
      if(++step>=tHist.rows.length){
        hRow=tHist.insertRow(step);
        hRow.innerHTML="<td> <td> <td> <td> ";
      }else
        hRow=tHist.rows[step];
      hRow.cells[0].innerText=" "+step;
      hRow.cells[1].innerText=" "+newSt;
      var finS=document.getElementById("finalS");
      for(var indF=2;indF<finS.rows.length;indF++){
        var rw=finS.rows[indF];
        if(parseInt(rw.cells[1].innerText)==newSt){
          hRow.cells[3].innerText=rw.cells[3].innerText;
          break;
        }
      }
      tScan.rows[2].cells[0].innerText="►";
      inp.finSt=newSt;
      inp.state=0;
      if((vSymb!="EOF")&&(byOther<0)){
        inp.selStart-=ls;
        inp.selEnd-=ls;
      }
    }
    if(oldSt!=newSt)
      _G.network.body.data.nodes.update(nToC);
//var etu=    _G.network.body.data.edges._data;
  }else{
    hRow=tHist.insertRow(tHist.rows.length);
    hRow.innerHTML="<td> <td> <td> <td> ";
    hRow.cells[0].innerText=" "+step;
    hRow.cells[3].innerText=" Error";
    hRow.cells[2].innerText=vSymb;
    hRow.cells[1].innerText=inp.state;
    inp.state=0;
//    tHistClear();
  }
  inp.step=step+1;
}
function showGraph(){
  textGraph();
const options = {
  nodes: {
    shape: "circle",
    size: 35,
    color: colors.cSV,
    font: {
      size: 14,
      color: "blue",
    },
    scaling:{min:5,max:10}
  },
  edges: {width: 1,length:12, arrows: {to: {enabled: true,scaleFactor:0.5}},color:"black"},
};
var nds=[];
var lnk=[];
var fins=-1;
var grTbl=document.getElementById("grScanner");
var rws=grTbl.rows.length-2;
for(var ir=0;ir<rws;ir++){
  nds.push({id:ir,label:""+ir});
  var lnks=grTbl.rows[ir+2].cells[3].innerText.split("->");
  var mark=lnks[0].trim();
  var toV;
  for(var il=0;il<lnks.length;il++){
    var lnn=lnks[il].trim();
    var ins=lnn.indexOf("\n");
    if(ins>0){
      toV=lnn.substring(0,ins).trim();
      var iToV=parseInt(toV);
      if(iToV<fins)
        fins=iToV;
      lnk.push({from:ir,to:toV,label:mark});
      mark=lnn.substring(ins+1).trim();
    }else
      lnk.push({from:ir,to:lnn,label:mark});
  }
}
  for(var il=-1;il>=fins;il-=1)
    nds.push({id:il,label:""+il,color:colors.fFV});
  var nodes = new vis.DataSet(nds);
  var edges = new vis.DataSet(lnk);
  var container = document.getElementById('vgScanner');
  var data = {
    nodes: nodes,
    edges: edges
  };
  _G.network = new vis.Network(container, data, options);
//  textGraph();
}

function gAllSteps(){
  if(document.getElementById("tInpStr").value.trim()==""){
    alert("Входная последовательность символов пуста, нечего делать.");
    return;
  }
//  _G.stepCnt=100;
  var tInt=parseInt(document.getElementById("tInterval").value);
  if(isNaN(tInt)||(tInt<100)||(tInt>10000))
    tInt=2000;
  document.getElementById("tInterval").value=tInt;
  gHistClear();
  _G.interval=setInterval(goGStep,tInt);
}
function goGStep(){
  var tH=document.getElementById("tHist");
  var l=tH.rows.length;
  var t=tH.rows[l-1].cells[1].innerText;
  var tt=parseInt(t);
  if((t=="Error")||(!isNaN(tt))&&(tt==-1))
    clearInterval(_G.interval);
  else
    gOneStep();
}
