/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope
    
    document.getElementById("Login").querySelector("a").addEventListener("click",()=>{
		document.getElementById("Login").hidden = true;
		document.getElementById("Registration").hidden = false;
		
	});
	
	document.getElementById("Registration").querySelector("a").addEventListener("click",()=>{
		document.getElementById("Login").hidden = false;
		document.getElementById("Registration").hidden = true;
		
	});
    
    
    document.getElementById("loginbutton").addEventListener('click', (e) => {
    var form = e.target.closest("form");
    var name = form.username.value;
    var pwd = form.password.value;
    if(pwd==null || name==null || pwd==='' || name===''){
		 document.getElementById("errorMsg").textContent = "You have to fill all the fields";
		 return;
	}
    if (form.checkValidity()) {
      makeCall("POST", 'CheckLogin', e.target.closest("form"),
        function(x) {
          if (x.readyState == XMLHttpRequest.DONE) {
            var message = x.responseText;
            switch (x.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("errorMsg").textContent = message;
                break;
              case 500: // server error
            	document.getElementById("errorMsg").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	 form.reportValidity();
    }
  });

})();

