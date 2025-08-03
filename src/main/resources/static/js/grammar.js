function Grammar(){
 this.rParser=new ruleParser();
}
Grammar.prototype={
  mongo:true,
  prefix:"",
  timerI:0,
  alarmI:0,
  extTH:0,
  extH:-100,
  tR:[],
  fR:[],
  wR:[],
  rulesChng:0,
  numNT:{},
  limit:1860,
  tc:0,
  rParser:0,
  gS:[],
  gP:[],
  cSynt:0,
  deltaLeft:0,
  deltaTop:0,
  deltaWidth:-6,
  deltaHeight:-6,
  wUser:"",
  divMnu:0,
  divExt:0,
  divRules:0, 
  divAny:0,
  divRes:0,
  txtCod:0,
  divTun:0,
//  divFnc:0,
  tabR:0,
  inpBox:0,
  selC:0,
  openR:0,
  openT:0,
  saveR:0,
  opnM:0,
  vIew:0,
  builD:0,
  ruN:0,
  tsT:0,
  tunE:0,
  rdeL:0,
  rnamE:0,
  rnorM:0,
  helP:0,
  ifr:undefined,
  testing:undefined,
  what:0,
  dTime:0,
  tplName:0,
  autoS:0,
  saveNN:0,
  inpT:"",
  tmpRule:"",
  mode:false,
  fio:"",
clear:function(){
 this.tR=[];
 this.fR=[];
 this.wR=[];
},
test:function(){
  let _=this._G;
  if(_.extH<0){
    _.extTH=document.getElementById("extT");
    _.extH=_.extTH.offsetHeight;
  }
  if(_.extH!==_.extTH.offsetHeight){
//    let diff=_.extH-_.extTH.offsetHeight;
    _.extH=_.extTH.offsetHeight;
    document.getElementById("rules").style.top=_.extTH.offsetTop +_.extTH.offsetHeight+16+"px";
    document.getElementById("rules").style.height=document.getElementById("mnu").offsetHeight-_.extH-32+"px";
  }
},
newRule:function(i,rS){
  let _=this;
  let f=true,ind,nd,c;
  ind=rS.search(/[^a-zA-Z0-9]/);
  if(rS[ind]===":")
    rS=rS.slice(0,ind)+" "+rS.slice(ind+1);
  else
    if((ind>0)&&(rS[ind]!==' '))
      rS=rS.slice(0,ind)+" "+rS.slice(ind);
  if(i<=_.tR.length){
    if(_.tR[i-1]===rS)
      f=false;
    else{
      _.tR[i-1]=rS;
      _.fR[i-1]=false;
    }
  }else{
    _.tR.push(rS);
    _.fR.push(false);
  }
  if(f){
    _.analyze();
//    _.showRules();
  }
},
newJRule:function(i,rS){
  let _=this;
  let ind;
  let r=rS["lPart"];
  rS=rS["rPart"];
  if(((ind=rS.indexOf(":"))>0)&&(rS.indexOf(" ")>=ind+1))
    rS=rS.slice(0,ind)+" "+rS.slice(ind+1);
  for(let i=0;i<rS.length;i++)
    r+=" "+rS[i];
  _.tR.push(r);
  _.fR.push(false);
},
getRules:function(){
	return tR;
},
analyze:function(){
  let _=this;
  _.emptyRem();
  _.toBrFree();
  _.toKvFree();
  _.calcProp();
  _.showRules();
  showNRules(_.wR,_.gS,_.gP);
    if(_.autoS===4)
      saveRules();
    else
      _.rulesChng=true;

},
addNT:function(nt){
  let _=this;
  let xnt=nt.split("!");
  nt = xnt[0];
  let ind = _.numNT[nt];
  if(ind===undefined){
    _.numNT[nt] = 0;
	ind = 0;
  } else {
      ind += 1;
      _.numNT[nt] = ind;	 
  }
  return nt + "!" + ind;
},
emptyRem:function(){
  let _=this;
  let i;
  for(i=0;i<_.tR.length;i++){
    if((_.tR[i].trim()==="")&&(i<_.tabR.rows.length)&&(_.tabR.rows[i+1].innerText.trim()==="")){
      _.tR.splice(i,1);
      _.tabR.deleteRow(i+1);
      continue;
    }
  }
},
toBrFree:function(){
  let _=this;
  _.numNT={};
  _.wR=[];_.gS=[];_.gP=[];
  let i,j,k,flag;
  let dr,beg,nr,nnr,newNT,newRule,newRP;
  let r,pr,rs,cnt,lp,er,n;
  for(i=0;i<_.tR.length;i++){
/*    if(_.tR[i].trim()===""){
      _.tR.splice(i,1);
      continue;
    }*/
    pr=_.rParser.parse(_.tR[i]);
    if((_.fR[i]=pr["Ok"])===true){
      r=pr["lRule"];
      dr=[];
      for(j=0;j<r.length;j++) {
        rs=r[j][1];
        if(rs!==""){
          if((k=_.gS.indexOf(rs))<0){
            k=_.gS.length;
            _.gS.push(rs);
            _.gP.push(r[j][0]);
          }
          dr.push(k);
        }
      }
      _.wR.push(dr);
    }
  }
  dr=null;
  flag=true;
	while(flag) {
	  flag=false;
	  i=0;
	  while(i<_.wR.length){
      r=_.wR[i];
      cnt=0;
      for(j=1;j<r.length;j+=1){
        er=_.gP[r[j]];
        if((er===4) && (cnt===0)){
          newRP = r.splice(j+1);
          r.pop();
          newRule=[r[0]];
          newRule=newRule.concat(newRP);
          _.wR.push(newRule);
          flag = true;
        }
        if(er===2)
          cnt+=1;
        if(er===3)
          cnt-=1;
      }
      i+=1;
    }
    i=0;
    while(i<_.wR.length){
      r=_.wR[i];
      lp=_.gS[r[0]];
      cnt=[];
      j=1;
      while(j<r.length){
        er=_.gP[r[j]];
        if(er===2)
          cnt.push(j);
        if(er===3) {
          beg=cnt.pop();
          nr=r.splice(beg);
          newNT=_.addNT(lp);
          if((n=_.gS.indexOf(newNT))<0){
            n=_.gS.length;
            _.gS.push(newNT);
            _.gP.push(1);
          }
          r.push(n);
          for(k=j-beg+1;k<nr.length;k++)
            r.push(nr[k]);
          nnr=nr.slice(1);
          nnr.splice(j-beg-1);
          newRule=[n];
          newRule=newRule.concat(nnr);
          _.wR.push(newRule);
          flag=true;
          cnt=[];
          j=-1;
        }
        j+=1;
      }
      i+=1;
    }
  }
},
toKvFree: function(){
  let _=this;
  let k,n,m,kv,dr,r,lp,smb,i=0,j,l,start,fin,tmp,newNT;
  let jr=[].concat(_.wR);
  while(i<jr.length){
    r=jr[i];
    lp=_.gS[r[0]];
//    smb=null;
    for(j=1;j<r.length;j+=1){
      if((_.gP[r[j]]===9) && (_.gP[r[j-1]]!==6)) {
        start=0;
        fin=-1;
        kv=_.gS[r[j]];
        if(kv==="?")
          fin=1;
        else if(kv==="+")
          start=1;
        else if(kv[0]==='{'){
          if(kv[1]!==',')
            start=parseInt(kv.substring(1));
          kv=kv.substring(kv.indexOf(",")+1);
          tmp=parseInt(kv);
          if(tmp>0)
            fin = tmp;
        }
        n=r[j-1];
        smb=_.gS[n];
        if(smb.indexOf("!")<0){
          dr = r.splice(j-1);
          lp=_.addNT(_.gS[r[0]]);
          if((n=_.gS.indexOf(lp))<0){
            n=_.gS.length;
            _.gS.push(lp);
            _.gP.push(1);
          }
          r.push(n);
          jr.push([n,dr[0]]);
          dr=dr.splice(1);
          smb=lp;
        } else {
          dr=r.splice(j);
        }
        if(start===0)
          jr.push([n]);
        if((start>=0) && (start*fin!==1)){
          if((fin>0) && (start>fin))
            fin+=start;
          if(start>0)
            for(k = 1; k < start; k++)
              r.push(n);
          if((fin>start) && (fin>1)){
            newNT=_.addNT(lp);
            if((m=_.gS.indexOf(newNT))<0){
              m=_.gS.length;
              _.gS.push(newNT);
              _.gP.push(1);
            }
            jr.push([m,n]);
            jr.push([m]);
            if(start>0)
              r.push(m);
            for(k = start+1; k < fin; k++)
              r.push(m);
          }
        }
        if(fin < 0){
          if(kv!=="*"){
            newNT=_.addNT(lp);
            if((m=_.gS.indexOf(newNT))<0){
              m=_.gS.length;
              _.gS.push(newNT);
              _.gP.push(1);
            }
            jr.push([m,n,m]);
            jr.push([m]);
            r.push(m);
          }else{
            for(l=0;l<jr.length;l++){
              if((jr[l][0]===n)&&(jr[l].length>1)){
                jr[l].push(n);
                break;
              }
            }
          }
        }
        if(dr.length>0){
          dr=dr.splice(1);
          r=r.concat(dr);
        }
        jr[i]=r;
      }
    }
  	i += 1;
  }
  _.wR=[].concat(jr);
  for(i=0;i<_.gP.length;i++)
    if((_.gP[i]>1)&&(_.gP[i]<5))
      _.gP[i]=10;
//  return jr;
},
calcProp:function(){
  let _=this;
  let i,j,k,m,n,w,r,q,f,typ,un,pP,smb,mMax,nonT,ns,noStN,nn,cwR,flUN,cntFUN,lNS;
  //разделение нетерминалов на лексические и синтаксические
  for(i=0;i<_.wR.length;i++){
    r=_.wR[i];
    if((r.length>1)&&(_.gP[r[0]]<2)){ //1 - начальное значение, 0 - подозревается, что лексический
      f=true;
      for(j=1;f&&(j<r.length);j++){
        typ=_.gP[r[j]];
        if((typ===1)&&(_.gS[r[j]].indexOf("!")<0))
          f=false;
        if((typ===8)||(typ===5))
          f=false;
      }
      if(f)
        _.gP[r[0]]=0;
      else
        _.gP[r[0]]=3;
    }
  }
  f=true;
  while(f){
    f=false;
    for(i=0;i<_.wR.length;i++){
      r=_.wR[i];
      if((r.length>1)&&(_.gP[r[0]]<2)){
        for(j=1;j<r.length;j++){
          if(_.gP[r[j]]===0){
            if(_.gS[r[j]].indexOf("!")<0){
              _.gP[r[0]]=3;
              f=true;
              break;              
            }
          }
          if(_.gP[r[j]]===3){
            _.gP[r[0]]=3;
            f=true;
            break;
          }
        }
      }
    }
  }
  for(i=0;i<_.gP.length;i++)
    if(_.gP[i]===0)
      _.gP[i]=4; //это лексический
  //добавление лексических правил вида Нетерминал : Строка для всех строк, встретившихся в правх частях правил
  //??? нужно предотвратить дублирование
  
  k=1;
  smb=[];
  for(i=0;i<_.gP.length;i++)
    if((_.gP[i]>1)&&(_.gP[i]<5)){
      k+=1;
      smb.push(i);
    }
//выявление начального нетерминала
  pP=new Array(k);
  for(i=0;i<k;i++){
    pP[i]=new Array(k);
  }
  cntFUN=2;
  flUN=true;
  while(flUN&&(cntFUN-->0)){
    flUN=false;
    k=smb.length+1;
    if(k>pP.length)
      k=pP.length;
    for(i=0;i<k;i++)
      for(j=0;j<k;j++)
        if(i===j)
          pP[i][j]=1;
        else
          pP[i][j]=0;
  //ставятс¤ отметки непосредственно из правил
    for(i=0;i<_.wR.length;i++){
      r=_.wR[i];
      n=smb.indexOf(r[0]);
      if(n>=0)
        for(j=1;j<r.length;j++){
          m=smb.indexOf(r[j]);
          if(m>=0)
            pP[n][m]=1;
        }
    }
  //выполн¤етс¤ транзитивное замыкание, отметка в строке ’ колонке ” означает, что ” выводитс¤ из ’
    f=true;
    while(f){
      f=false;
      for(i=0;i<k;i++){
        for(j=0;j<k;j++){
          if(pP[i][j]>0){
            for(m=0;m<k;m++){
              if(pP[j][m]!==0){
                if(pP[i][m]===0)
                  f=true;
                pP[i][m]=1;
              }
            }
          }
        }
      }
    }
  //вычисл¤етс¤ начальный нетерминал системы правил как символ, из которого выводитс¤ наибольшее количество других символов
    noStN=-1;
    n=k-1;
    mMax=0;
    for(i=0;i<n;i++){
      m=0;
      for(j=0;j<n;j++){
        m+=pP[i][j];
  //      m[lim][j]+=m[i][j];
      }
      pP[i][n]=m;
      if((m>mMax)&&(_.gS[smb[i]].indexOf("!")<0)&&(_.gP[smb[i]]>1)&&(_.gP[smb[i]]<4)){
        mMax=m;
        noStN=smb[i];
      }
    }
    if(noStN<0){
      if((m=_.gS.indexOf("Word!"))<0){
        nn=_.wR.length;
        noStN=smb.length;
        m=_.gS.length;
        smb.push(m);
        _.gS.push("Text!0");
        _.gP.push(3);
        noStN=m+1;
        _.gS.push("Text!");
        _.gP.push(2);
        _.wR.push([m+1,m,m+1]);
        _.wR.push([m+1]);
        k+=1;n+=1;
        if(k>pP.length)
          pP.push(new Array(k));
        if(k>pP.length)
          pP.push(new Array(k));
        pP[n][n-1]=1;
        for(i=0;i<_.gP.length;i++)
          if((_.gP[i]===4)&&(_.gS[i].indexOf("!")<0))
            _.wR.push([m,i]);
          else if(_.gP[i]===5){
            f=true;
            for(j=0;j<nn;j++)
              if(_.wR[j].indexOf(i)>0){
                f=false;
                break;
              }
            if(f)
              _.wR.push([m,i]);
          }
      } else{
        _.gP[m]=2;
        noStN=m;
      }
    } else
      _.gP[noStN]=2;
    nonT=_.gS[noStN];
    if(smb.indexOf(noStN)<0)
      smb.push(noStN);
    pP[n][smb.indexOf(noStN)]=1;
  //в последней строке массива m формируетс¤ признак достижимости из начального нетерминала
    f=true;
    while(f){
      f=false;
      for(i=0;i<n;i++)
        if(pP[n][i]===1){
          pP[n][i]=2;
          for(j=0;j<n;j++)
            if((pP[i][j]>0)&&(pP[n][j]===0)){
              pP[n][j]=1;
              f=true;
            }
        }
    }
    let un=[];
  //формируетс¤ список недостижимых нетерминалов
    for(i=0;i<n;i++)
      if((pP[n][i]===0)&&(_.gP[smb[i]]!==4))
        un.push(smb[i]);
  //список пополняется бесплодными нетерминалами
    cwR=[].concat(_.wR);
    f=true;
    while(f){
      f=false;
      i=0;
      while(i<cwR.length){
        r=[].concat(cwR[i]);
        for(j=1;j<r.length;j++){
          if(_.gP[r[j]]>4)
            r.splice(j--,1);
        }
        if(r.length===1){
          k=r[0];
          f=true;
          j=0;
          while(j<cwR.length){
            r=[].concat(cwR[j]);
            while((q=r.indexOf(k))>=0){
              if(q===0){
                cwR.splice(j--,1);
                r=[];
              }else if(q>0){
                r.splice(q,1);
                cwR[j]=r;
              }
            }
            j+=1;
          }
        }
        i+=1;
      }
    }
    if(cwR.length>0)
      flUN=true;
    for(i=0;i<cwR.length;i++)
      if(un.indexOf(cwR[i][0])<0){
        _.gP[cwR[i][0]]=0;
      }
    for(i=0;i<_.gP.length;i++)
      if(_.gP[i]<2)
        un.push(i);
  //удаление правил с недостижимыми символами
    if(un.length>0)
      for(i=0;i<_.wR.length;i++){
        r=_.wR[i];
        for(j=0;j<r.length;j++){
          m=r[j];
          if(un.indexOf(m)>=0){
            ns=r[0];
            if(_.gP[ns]===2)
              flUN=true;
            _.wR.splice(i,1);
//            if(_.gS[ns].indexOf("!")<0)
              for(m=0;m<_.wR.length;m++)
                if(_.wR[m][0]===ns){
                  ns=-1;
                  break;
                }
            if((ns>=0)&&(un.indexOf(ns)<0))
              un.push(ns);
            j=r.length;
            i=-1;
            if((k=smb.indexOf(ns))>=0){
              smb.splice(k,1);
            }
            break;
            }
        }
      }
  }
//  return;
  for(i=0;i<_.wR.length;i++){
    r=_.wR[i];
    j=r[0];
    if((r.length>1)&&(_.gS[j].indexOf("Text!")<0)&&((_.gP[j]===3)||(_.gP[j]===2))){
      for(j=1;j<r.length;j++){
        if((_.gP[r[j]]===5)||(_.gP[r[j]]===6)){
          if((k=_.gS.indexOf("Word!"))<0){
            k=_.gS.length;
            _.gS.push("Word!");
            _.gP.push(4);
          }
          w=r[j];
          f=true;
          for(m=_.wR.length-1;m>0;m-=1){
            if(_.wR[m][0]!==k)
              break;
            if(_.wR[m][1]===w){
              f=false;
              break;
            }
          }
          if(f)
            _.wR.push([k,w]);
        }
      }
    }
  }
  if(((k=_.gS.indexOf("Word!"))>=0)&&(_.gP[k]===2)){
    _.gP[k]=4;
    m=_.gS.length;
    noStN=m;
    _.gS.push("Text!");
    _.gP.push(2);
    n=_.wR.length;
    _.wR.push([m,k,m]);
    _.wR.push([m]);
    for(i=0;i<_.gP.length;i++)
      if((i!==k)&&(_.gP[i]===4)&&(_.gS[i].indexOf("!")<0)){
        f=true;
        for(j=0;j<n;j++)
          if((_.wR[j][0]===k)&&(_.wR[j][1]===i)){
            f=false;
            break;
          }
        if(f)
          _.wR.push([k,i]);
      }
  }
//перенос правил дл¤ начального нетерминала в начало списка правил
  k=0;
  for(i=_.wR.length-1;i>k;i--){
    m=_.wR[i][0];
    if(m===noStN){
      r=[].concat(_.wR[i]);
      _.wR.splice(i++,1);
      _.wR.unshift(r);
      k+=1;
    }
  }
  smb=[noStN];
  cwR=[];
  k=0;
  lNS=[];
  while(k<smb.length){
    m=smb[k];
    for(i=0;i<_.wR.length;i++)
      if(_.wR[i][0]===m){
        r=[].concat(_.wR[i]);
        _.wR.splice(i--,1);
        if(_.gP[r[0]]!==4)
          cwR.push(r);  //!!!
        else{
          ns=_.gS[r[0]];
          if((lNS.indexOf(ns)<0)){//((ns=_.gS[r[0]]).indexOf("!")<0)&&
            lNS.push(ns);
            ns+=" ";
            for(j=0;j<_.tR.length;j++){
              if(_.tR[j].indexOf(ns)===0){
                let rp=_.rParser.parse(_.tR[j]);
                rp=rp["lRule"];
                f=true;
                for(let kk=1;kk<rp.length;kk++){
                  if((rp[kk][0]===7)&&(kk<rp.length-1)){
                    f=false;
                    break;
                  }
                }
                if(f){
                  r=[r[0],_.gS.length];
                  _.gS.push(_.tR[j].substr(ns.length));
                  _.gP.push(6);
                  cwR.push(r);  //!!!
                }
              }
            }
          }/*else{
            let xf=true;
            for(let xi=0;xf&&(xi<cwR.length);xi++){
              let xr=cwR[xi];
              let rrr=r[0];
              if((xr[0]===rrr)&&(xr.length===r.length)){
                let xxf=true;
                for(let xj=1;xj<r.length;xj++)
                  if(xr[xj]!==r[xj]){
                    xxf=false;
                    break;
                  }
                if(xxf){
                  xf=false;
                  break;
                }
              }
            }
            if(xf)
              cwR.push(r);
            }*/
        }
        for(j=1;j<r.length;j++)
          if(smb.indexOf(r[j])<0)
            smb.push(r[j]);
      }
    k+=1;
  }
//  let fl=true;
  for(i=0;i<_.wR.length;i++){
    r=[].concat(_.wR[i]);
    if(!((_.gP[r[0]]===4)&&(_.gS[r[0]].indexOf("!")>0)))
      if(nonT==="Text!")
        cwR.push(r);  //!!!
      else{
        ns=_.gS[r[0]]+" ";
        k=ns.length;
        for(j=0;j<_.tR.length;j++){
          if(_.tR[j].substring(0,k)===ns){
            w=_.tR[j].substring(k).trim();
            if((m=_.gS.indexOf(w))<0){
              m=_.gS.length;
              _.gS.push(w);
            }
            cwR.push([r[0],m]);
            break;
          }
        }
      }
    else if(_.gS[r[0]]==="Word!")
      cwR.push(r);  //!!!
    for(j=0;j<r.length;j++)
      if(smb.indexOf(r[j])<0)
        smb.push(r[j]);
  }
/*  for(i=0;i<_.gS.length;i++)
    if((smb.indexOf(_.gS[i])<0)&&(_.gP[i]>1)&&(_.gP[i]<5))
      _.gP[i]=-1;*/
  f=true;
  while(f){
    f=false;
    for(i=0;i<cwR.length;i++){
      if((cwR[i].length===2)&&((_.gP[cwR[i][1]])===3)){
        k=cwR[i][1];
        m=0;
        for(j=0;j<cwR.length;j++){
          r=cwR[j];
          for(let kx=0;kx<r.length;kx++)
            if(r[kx]===k){
              m+=1;
              if(kx===0)
                n=j;
            }
        }
        if(m===1){
          cwR[i][1]=cwR[n][1];
          for(m=2;m<cwR[n].length;m++)
            cwR[i].push(cwR[n][m]);
          cwR.splice(n,1);
          f=true;
        }
      }
    }
  }
  for(i=0;i<cwR.length;i++){
    r=cwR[i];
    for(j=i+1;j<cwR.length;j++){
      ns=cwR[j];
      if(r.length===ns.length){
        f=true;
        for(k=0;k<r.length;k++)
          if(r[k]!==ns[k]){
            f=false;
            break;
          }
        if(f){
          cwR.splice(j,1);
          j-=1;
        }
      }
    }
  }
  if((cwR.length>2)||(_.gS[cwR[0][0]].substr(0,5)!=="Text!")){
    _.wR=cwR;
    document.getElementById("bUild").classList.remove("grSp");
    document.getElementById("rSave").classList.remove("grSp");
    document.getElementById("rClr").classList.remove("grSp");
  }else{
    _.wR=[];
    document.getElementById("bUild").classList.add("grSp");
    document.getElementById("rSave").classList.add("grSp");
    document.getElementById("rClr").classList.add("grSp");
  }
},
toFBrFree:function(r){
  let _=this;
  let i,j,b,bb,e,f,l,x,ppr,pp="",act="",ic;
  let pr=_.rParser.parse(r[0]+r[1]);
  pr=pr["lRule"];
  i=pr.length-1;
  if(pr[i][0]===7){
    act=pr[i][1];
    pr.splice(i,1);
  }
  let st=[];
  for(i=1;i<pr.length;i++){
    x=pr[i];
    if(x[1]==="(")
      st.push(i);
    if((x[1]===")")&&(i<pr.length-1)&&(pr[i+1][1].search(/{[,0-9]/)<0))
      st.pop();
    if((x[0]===9)&&(x[1].search(/{[,0-9]/)>=0)){
      ppr=x[1].substr(1);
      f=st.length>0?st.pop():i-1;
      l=i+1;
      b=0;
      if(ppr.charAt(0)!==",")
        b=parseInt(ppr)|0;
      ic=ppr.indexOf(",");
      ppr=ppr.substr(ic+1);
      e=parseInt(ppr)|0;
      if(b===0)
        if((e===0)||(isNaN(e)))
          x[1]="*";
        else
          x[1]="?";
      else
        if(e===0)
          x[1]="+";
        else{
          x[1]="?";
          if(b===e){
            pr.splice(i,1);
            b-=1;
          }
//          i+=(ic<0?-1:0);
          i=e;
          for(bb=0;bb<b;bb++)
            for(j=f;j<l-1;j++)
              pr.splice(++i,0,pr[j]);
        }
      for(b++;e>b;b++){
        for(j=f;j<l;j++)
          pr.splice(++i,0,pr[j]);
      }
    }
  }
  for(i=1;i<pr.length;i++)
    pp+=pr[i][1]+" ";
  return [pr[0][1],pp,act];
},
showRules:function(){
  let _=this;
  let i,j,k,rs,pr,r,t,m,fl;
  let n=_.tR.length+1;
  document.getElementById("bUild").classList.remove("grSp");
  document.getElementById("rClr").classList.remove("grSp");
  while(_.tabR.rows.length>n+1)
    _.tabR.deleteRow(n);
  for(i=0;i<_.tR.length;i++){
    rs=_.tR[i];
    pr=_.rParser.parse(_.tR[i]);
    r=pr["lRule"];
    fl=pr["Ok"];
    if(fl){
      if((r.length>0)&&(r[0].length>1)){
        k=_.gS.indexOf(r[0][1]);
        t=_.gP[k]===4;
        for(j=0;j<r.length;j++){
          k=_.gS.indexOf(r[j][1]);
          m=r[j][0]=_.gP[k];
          if(t){
            if(m===5){
              fl=false;
              break;
            }
          }else
            if(m===6){
              fl=false;
              break;
            }
        }
      }
    }
    if(!fl)
      m=0;
    if(r.length>0)
      showRule(i+1,r,fl,rs);
  }
}
}