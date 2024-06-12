/**
 * Scrypt to manage the home page 
 */

 (function(){
	 
	 window.addEventListener("load",()=>{
		 show();
		 logout();
	 },false);
	 
	  let draggedDoc;
	  let draggedFold;
	  let isDoc;
	 
	 function show(){
		
		makeCall("GET",'GetFolders',null,
		function(x){
			if(x.readyState == XMLHttpRequest.DONE){
				var myList = document.getElementById("folderTree");
				var message = x.responseText;
				switch(x.status){
					case 401:
						sessionStorage.removeItem("username");
						window.location.href = "login.html";
						break;
					case 500:
						var text = document.createElement("p");
						text.textContent = message;
						myList.appendChild(text);
						break;
					case 200:
						var folders = JSON.parse(message);
						if(folders.length == 0){
							var show = document.createElement("p");
							show.textContent = "No folders yet";
							myList.appendChild(show);
							changeButtonPos(myList);
							var bin = document.getElementById("bin");
		                    makeBinDroppable(bin);
							break;
						}
						else{
						//Clean the tree of folders(div-->id=folders)
						document.getElementById("welcome").innerHTML="";
						document.getElementById("folderTree").innerHTML = "";
						
						//Clean the list of the subfolders of a selected folder(div-->id=folderContent)
						document.getElementById("folderName").innerHTML = "";
						document.getElementById("subfolders").innerHTML = "";
						document.getElementById("DocumentsTitle").innerHTML = "";
						
						//Clean the list of the documents of a selected folder(div-->id = folderContent)
						document.getElementById("docs").innerHTML = "";
						document.getElementById("err").innerHTML = "";
						
						//Clean the data of a document(div-->id=docinfo)
						document.getElementById("docName").innerHTML = "";
						document.getElementById("folder").innerHTML = "";
						document.getElementById("author").innerHTML = "";
						document.getElementById("type").innerHTML = "";
						document.getElementById("date").innerHTML = "";
						document.getElementById("sum").innerHTML = "";
						document.getElementById("back").innerHTML = "";
					    document.getElementById("docForm").hidden = true;
					    
						var welcome = document.getElementById("welcome");
						var user = sessionStorage.getItem("username");
						welcome.textContent = "Welcome Back " + user;
						myList.innerHTML = "";
						document.getElementById("navigateToHome").innerHTML = "";
						folders.forEach(function(fold){
								var folderElement = createFolderElement(fold);
								myList.appendChild(folderElement);
								
							});
					    changeButtonPos(myList);
		        		var bin = document.getElementById("bin");
		                makeBinDroppable(bin);
		        		
							
						}
				}
			}
		}
		);
	 }
	 
	 function createFolderElement(folder){
		 //Create a folder <li> with a <span> that has an event listener
		 var folderItem = document.createElement("li");
		 folderItem.setAttribute("folderID",folder.Id);
		 var folderName = document.createElement("span");
		 folderName.draggable = true;
		 makeDroppableFolder(folderName);
		 folderName.addEventListener("click",(e)=>getFolderContent(e,null)) 
		 
		 //Create the buttons for adding folder and document
		 var folderButton  = document.createElement("button");
		 folderButton.textContent = "AddFolder";
		 var docButton  =document.createElement("button");
		 docButton.textContent = "AddDoc";
		 
		 //Create the text box that appears when clicking on the addFolder button 
		 var textBox = document.createElement("input");
		 textBox.type = 'text';
		 textBox.hidden = 'true';
		 textBox.setAttribute("rootFolder", folder.Id);
		 
		 //Style for the buttons
		 docButton.style.marginLeft = "25px";
		 folderButton.style.marginLeft = "25px";
		 addFolder(folderButton);
		 addDocument(docButton);
		 
		 //Add all the elements to the li item created for the folder
		 folderItem.appendChild(folderName);
		 folderItem.appendChild(folderButton);
		 folderItem.appendChild(docButton);
		 folderItem.appendChild(textBox);
		 folderName.textContent = folder.name;
		 if(folder.subfolders && folder.subfolders.length>0){
			 var subfolders = document.createElement("ul");
			 folder.subfolders.forEach(function(sub){
				 var subfolderEl = createFolderElement(sub);
				 subfolders.appendChild(subfolderEl);
			 });
			 folderItem.appendChild(subfolders);
		 }
		 return folderItem;
	 }
	 
	 function addFolder(button){
		 button.addEventListener("click",(e)=>{
			 e.stopPropagation();
			 var myList = document.getElementById("folderTree");
			 var folder = e.target.closest("li")
			 var button = e.target.closest("button");
			 button.hidden = true;
			 input = folder.querySelector("input");
		 	 input.hidden = false;
		 	 input.style.marginLeft = "25px";
			 input.addEventListener("keypress",(e)=>{
                 e.stopPropagation();
				 if(e.key === 'Enter'){
					 if(input.value.trim()==="" || input.value==null){
						 document.getElementById("err").textContent = "You must give a name for your folder";
						 input.hidden = true;
						 folder.querySelector("button").hidden = false;
					 }
					 if(input.value.length>45){
						document.getElementById("err").textContent = "No more than 45 characters";
						 input.hidden = true;
						 folder.querySelector("button").hidden = false; 
					 }
					 else{
						 makeCall("POST", "AddFolder?rootFolder="+input.getAttribute("rootFolder")+"&foldername="+input.value,null,
						 function(x){
							 if(x.readyState === XMLHttpRequest.DONE){
								 var message = x.responseText;
								 switch(x.status){
									 case 401:
										sessionStorage.removeItem("username");
						                window.location.href = "login.html";
						                break;
						             case 400:
										 document.getElementById("err").textContent = message;
								         break;
								     case 500:
										document.getElementById("err").textContent = message;
								        break;
								     case 200:
										var folders = JSON.parse(message);
										document.getElementById("folderTree").innerHTML = "";
										folders.forEach(function(fold){
								            var folderElement = createFolderElement(fold);
								            myList.appendChild(folderElement);
								
						 	            });
						 	            changeButtonPos(myList);
										break;    
								 }
							 }
						 });
					 }
				 }
				 
			 });
		 });
	 }
	 
	 function addDocument(docButton){
		 docButton.addEventListener("click",(e)=>{
			 e.stopPropagation();
			 var foldID = e.target.closest("li").getAttribute("folderID");
			 document.getElementById("docForm").hidden = false;
			 document.getElementById("newDoc").addEventListener("click",(e)=>{
				 e.stopPropagation();
				 var form  = e.target.closest("form");
				 var name = form.docname.value;
				 var type = form.type.value;
				 var sum = form.sum.value;
				 if(name==null || type==null || sum==null || name==='' || type==='' || sum===''){
					 document.getElementById("docForm").hidden = true;
					 document.getElementById("err").textContent = "You must fill all the fields of the form";
					 return;
				 }
				 if(sum.length>200 || name.length>45 || type.length>45){
					 document.getElementById("docForm").hidden = true;
					 document.getElementById("err").textContent = "Too many characters";
					 return;
				 }
				 else{
					 makeCall("POST","AddDocument?docname="+name  + "&type=" + type + "&folder=" + foldID + "&summary="+sum,null,
					 function(x){
						 var message = x.responseText;
						 if(x.readyState === XMLHttpRequest.DONE){
							 switch(x.status){
								 case 401:
									sessionStorage.removeItem("username");
						            window.location.href = "login.html";
						            break;
						         case 500:
									 document.getElementById("err").textContent = message;
									 document.getElementById("docForm").hidden = true;
									 break;
							     case 400:
									 document.getElementById("err").textContent = message;
									 document.getElementById("docForm").hidden = true;
									 break;
							      case 200:
									  getFolderContent(e,foldID);
									  break;
									 
							 }
						 }
					 }
					 );
				 }
				 
			 });
		 });
	 }
	 function getFolderContent(e,foldId){
		 e.stopPropagation();
		 var folderID;
		 var folder;
		 if(foldId!=null){
			 folderID = foldId;
		 }
		 else{
		    folder = e.target.closest("li");
		    folderID  = folder.getAttribute("folderID");
		 }
		 makeCall("GET","GetFolderContent?folderID=" + folderID,null,
		 function(x){
			 var message = x.responseText;
			 if(x.readyState == XMLHttpRequest.DONE){
				 switch(x.status){
					 case 401:
						sessionStorage.removeItem("username");
						window.location.href = "login.html";
						break;
					 case 400:
						document.getElementById("err").textContent = message;
						break;
					 case 500:
						document.getElementById("err").textContent = message;
						break;
					 case 404:
						document.getElementById("err").textContent = message;
						break;
					case 200:
						//Hide the tree of folders
						document.getElementById("folders").hidden = true;
						//Clean the list of the subfolders of a selected folder
						document.getElementById("folderName").innerHTML = "";
						document.getElementById("subfolders").innerHTML = "";
						//Clean the list of the documents of a selected folder
						document.getElementById("docs").innerHTML = "";
						document.getElementById("err").innerHTML = "";
						document.getElementById("folderContent").hidden  = false;
						//Hide the data of a document
						document.getElementById("docinfo").hidden = true;
						document.getElementById("docForm").hidden = true;
						//document.getElementById("back").innerHTML = "";
						document.getElementById("back").hidden = true;
						document.getElementById("navigateToHome").hidden = false;
						
						var content = JSON.parse(message);
						var subfold = content.folders;
						var docs = content.documents;
						var myList = document.getElementById("subfolders");
						
						//Show all the subfolders
						subfold.forEach(function(sub){
							var subItem = document.createElement("li");
							subItem.setAttribute("folderID",sub.Id);
							subItem.addEventListener("click",(e)=>getFolderContent(e));
							var subName = document.createElement("span");
							subName.textContent = sub.name;
							subItem.appendChild(subName);
							myList.appendChild(subItem);
						});
						var docList = document.getElementById("docs");
						document.getElementById("DocumentsTitle").textContent = "Documents: ";
						
						//Show all the documents
						docs.forEach(function(doc){
							var docItem = document.createElement("li");
                            docItem.setAttribute("folderID",folderID)
							docItem.setAttribute("docID",doc.Id);
							dragDoc(docItem);
							docItem.addEventListener("click",(e)=>getDocumentContent(e));
							var docName = document.createElement("span");
							docName.textContent = doc.Name;
							docItem.appendChild(docName);
							docList.appendChild(docItem);
						});
						var nav = document.getElementById("navigateToHome");
						nav.textContent = "Go To Home Page";
						nav.addEventListener("click", (e) =>{
							     e.stopPropagation();
							     document.getElementById("folders").hidden = false;
							     document.getElementById("folderContent").hidden = true;
							     document.getElementById("navigateToHome").hidden = true;
						});
				 }
			 }
		 }
		 );
		 
	 }
	 function changeButtonPos(myList){
		 var newRow = document.createElement("li");
		 var foldButton  = document.createElement("button");
		 var textBox = document.createElement("input");
		 textBox.type = 'text';
		 textBox.hidden = 'true';
	     foldButton.textContent = "AddRootFolder";
		 foldButton.style.marginLeft = "25px";
		 textBox.setAttribute("rootFolder", -1);
		 myList.appendChild(newRow);
		 newRow.appendChild(foldButton);
		  newRow.appendChild(textBox);
		 addFolder(foldButton);
		 
		 
	 }
	 function getDocumentContent(e){
		 e.stopPropagation();
		 var doc = e.target.closest("li");
		 var docID = doc.getAttribute("docID");
		 console.log(docID);
		 makeCall("GET","GetDocumentInfo?docID=" + docID,null,
		 function(x){
			 var message = x.responseText;
			 if(x.readyState == XMLHttpRequest.DONE){
				 switch(x.status){
					 case 401:
						sessionStorage.removeItem("username");
						window.location.href = "login.html";
						break;
					 case 400:
						document.getElementById("err").textContent = message;
						break;
					 case 500:
						document.getElementById("err").textContent = message;
						break;
					 case 200:
						//Hide the tree of folders
						document.getElementById("folders").hidden = true;
						//Hide the list of the subfolders and documents of a selected folder
						document.getElementById("folderContent").hidden = true;
						
						document.getElementById("docinfo").hidden = false;
						document.getElementById("docForm").hidden = true;
						document.getElementById("back").hidden = false;
						document.getElementById("navigateToHome").hidden = true;
						var resp = JSON.parse(message);
						var doc = resp.document;
						var folderName = resp.fold;
  					    console.log(doc);
						console.log(folderName);
						document.getElementById("docName").textContent = doc.Name;
						document.getElementById("folder").textContent = "Folder: " +  folderName.name;
						document.getElementById("author").textContent ="Author: " + doc.Author;
						document.getElementById("type").textContent ="Type: " + doc.Type;
						document.getElementById("date").textContent = "Creation date: "  + doc.CreationDate;
						document.getElementById("sum").textContent = "Summary: " + doc.Summary;
						document.getElementById("back").textContent = "Go Back";
						document.getElementById("back").addEventListener("click",(e)=>{
							e.stopPropagation();
							document.getElementById("docinfo").hidden = true;
							document.getElementById("folderContent").hidden = false;
							document.getElementById("navigateToHome").hidden = false;
							document.getElementById("back").hidden = true;
							
						});
					    

						 
					
						 
				 }
			 }
			 
		 });
	 }
	 
	 function dragDoc(doc){
		 doc.draggable = true;
		 doc.addEventListener("dragstart",(e)=>{
			 draggedDoc = e.target.closest("li");
			 isDoc = true;
			 e.stopPropagation();
		 });
		 doc.addEventListener("dragover",(e)=>{
			document.getElementById("folderContent").hidden = true;
		    document.getElementById("folders").hidden = false;
			document.getElementById("navigateToHome").hidden = true;
			e.preventDefault();
			e.stopPropagation();
			 
		 });
		
	 }
	 function makeDroppableFolder(folder){
		 folder.addEventListener("dragover",(e)=>{
			 e.stopPropagation();
			if(isDoc===true && draggedDoc.getAttribute("folderID")!==folder.closest("li").getAttribute("folderID")){
			   e.target.className = "selected";
			   e.preventDefault(); 
			}
		 });
		 folder.addEventListener("dragleave",(e)=>{
			e.stopPropagation();
			e.target.className = ""; 
		 });
		 
		 folder.addEventListener("dragstart",(e)=>{
			e.stopPropagation();
			isDoc = false;
			draggedFold = e.target.closest("li"); 
		 });
		  
		 folder.addEventListener("drop",(e)=>{
			e.stopPropagation();
			e.target.className = "";
			isDoc = false;
			docId = draggedDoc.getAttribute("docID");
			foldId = e.target.closest("li").getAttribute("folderID"); 
			makeCall("GET","MoveToFolder?folderID="+foldId + "&docID=" + docId,null,function(x){
				var message = x.responseText;
				if(x.readyState === XMLHttpRequest.DONE){
					switch(x.status){
						case 401:
							sessionStorage.removeItem("username");
							window.location.href = "login.html";
							break;
						case 400:
							document.getElementById("err").textContent = message;
							break;
						case 500:
							document.getElementById("err").textContent = message;
							break;
						case 200:
							getFolderContent(e,foldId);
							break;
					}
				}
			});
		 });
	 }
	 function makeBinDroppable(bin){
		 bin.addEventListener("dragover",(e)=>{
			 e.stopPropagation();
			 e.preventDefault();
			 e.target.className = "selected";
		 });
		 bin.addEventListener("dragleave",(e)=>{
			 e.stopPropagation();
			 e.target.className = "";
		 });
		 
		 bin.addEventListener("drop",(e)=>{
			e.target.className = "";
			if(confirm("Do you want to proceed?")===true){
				if(isDoc===true){
					var docID = draggedDoc.getAttribute("docID");
					makeCall("GET","DeleteDocument?docID=" + docID,null,
					function(x){
						var message = x.responseText;
						if(x.readyState===XMLHttpRequest.DONE){
							switch(x.status){
								case 401:
									sessionStorage.removeItem("username");
							        window.location.href = "login.html";
							        break;
							    case 400:
									document.getElementById("err").textContent = message;
							        break;
							    case 500:
								    document.getElementById("err").textContent = message;
							        break;
							    case 404:
									document.getElementById("err").textContent = message;
						 	        break;
						 	    case 200:
									document.getElementById("err").textContent = "Document deleted";
						 	        break;
							}
						}
						
					});
				}
				else{
					var folderID = draggedFold.getAttribute("folderID");
					makeCall("GET","DeleteFolder?folderID=" + folderID,null, 
					function(x){
						var message = x.responseText;
						if(x.readyState===XMLHttpRequest.DONE){
							switch(x.status){
								case 401:
									sessionStorage.removeItem("username");
							        window.location.href = "login.html";
							        break;
							    case 400:
									document.getElementById("err").textContent = message;
							        break;
							    case 500:
								    document.getElementById("err").textContent = message;
							        break;
							    case 404:
									document.getElementById("err").textContent = message;
						 	        break;
						 	    case 200:
									document.getElementById("err").textContent = "Folder deleted";
									var parent = draggedFold.parentElement;
					                parent.removeChild(draggedFold);
						 	        break;
							}
						}
							
					});
					
					
				}
			}
			else{
				return;
			}
			
		 });
	 }
	 
		 
	 function logout(){
		 document.getElementById("logout").addEventListener("click",()=>{
			makeCall("GET",'Logout',null,
			function(x){
				if(x.readyState == XMLHttpRequest.DONE){
					if(x.status === 200){
					  sessionStorage.removeItem("username");
					  window.location.href = "login.html";
					}
					else{
						console.log("Couldn't log out");
					}
				}
			});
		 });
	}	 
	 
 })();
 