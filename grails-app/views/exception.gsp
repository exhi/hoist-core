%{--
  - This file belongs to Hoist, an application development toolkit
  - developed by Extremely Heavy Industries (www.xh.io | info@xh.io)
  -
  - Copyright © 2018 Extremely Heavy Industries Inc.
  --}%

<g:render template="/includes/header"/>

<body>
    <div class='xh-static-shell'>
        <div class='xh-static-header'>Application Error</div>
        <div class="xh-static-shell-inner">
            ${exception.message}<br><br>
            Additional details:
            <div class="xh-exception-json">${exceptionAsJSON}</div>
        </div>
    </div>
</body>
</html>
