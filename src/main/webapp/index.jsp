<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Zypher Stegano Tool</title>
  <script src="https://cdn.jsdelivr.net/npm/twind@0.16.17/twind.min.js"></script>
  <link href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css" rel="stylesheet">

  <style>
    html, body {
      margin: 0;
      padding: 0;
      height: 100%;
      overflow: hidden;
      background-color: #000;
      color: #33ff33;
      font-family: 'Courier New', monospace;
    }

    canvas {
      position: fixed;
      top: 0;
      left: 0;
      z-index: 0;
    }

    .glow {
      box-shadow: 0 0 8px #33ff33;
    }

    textarea, input[type="file"] {
      background-color: #111;
      border: 1px solid #33ff33;
      color: #33ff33;
    }

    .btn {
      background-color: #000;
      border: 1px solid #33ff33;
      color: #33ff33;
      padding: 0.5rem 1rem;
      border-radius: 0.375rem;
      transition: background 0.2s;
    }

    .btn:hover {
      background-color: #33ff33;
      color: #000;
    }

    .content-container {
      position: relative;
      z-index: 1;
    }
  </style>
</head>
<body>

  <canvas id="matrixCanvas"></canvas>

  <div class="content-container min-h-screen flex items-center justify-center p-4">
    <div class="w-full max-w-2xl bg-black p-6 rounded-xl glow">
      <div class="text-center mb-4">
		  <h1 class="text-2xl font-bold text-green">ZYPHER - STEGANO TOOL</h1>
		  <div class="flex flex-col items-center mt-1 space-y-0.5">
		    <div class="w-2/3 h-0.5 bg-green-500"></div>
		     <p class="text-center mb-6">Hide your data into any photoes</p>
		    <div class="w-2/3 h-0.5 bg-green-500"></div>
		  </div>
	  </div>
      
      
     

      <form id="steganoForm" action="stegano" method="post" enctype="multipart/form-data">
        <input type="hidden" name="action" id="formAction" value="embed"> <%-- This will be dynamically set --%>

        <div class="mb-4">
          <label class="block mb-1">Upload Image</label>
          <input type="file" name="uploadImg" class="w-full px-3 py-2 rounded" required>
        </div>

        <div class="mb-4">
          <label class="block mb-1">Secret Message</label>
          <textarea name="secretMessage" rows="4" placeholder="Type secret message..." class="w-full px-3 py-2 rounded"></textarea>
        </div>
        <div class="flex justify-between mb-4 gap-2">
	        <button type="button" class="btn w-full" onclick="setFormAction('embed')">Embed</button>
	        <button type="button" class="btn w-full" onclick="setFormAction('extract')">Extract</button>
          	<button type="reset" class="btn w-full">Reset</button>
        </div>
      </form>

      <div class="mb-4">
        <p class="mb-1">Result Image</p>
        <div class="w-full h-40 bg-[#111] border border-[#33ff33] rounded flex items-center justify-center text-sm">
          <%-- Display encoded image if available --%>
          <% 
            byte[] encodedImageData = (byte[]) request.getAttribute("encodedImageBytes");
            if (encodedImageData != null) {
                String base64Image = java.util.Base64.getEncoder().encodeToString(encodedImageData);
          %>
              <img src="data:image/png;base64,<%= base64Image %>" alt="Encoded Image" class="max-w-full max-h-full object-contain" />
          <%
            } else if (request.getAttribute("extractedMessage") != null) {
          %>
              <p>Extracted Message: <%= request.getAttribute("extractedMessage") %></p>
          <%
            } else {
          %>
              [Encoded Image/Extracted Message will appear here]
          <%
            }
          %>
        </div>
      </div>

      <% if (request.getAttribute("encodedImageBytes") != null) { %>
      <div class="text-center mb-4">
        <form action="DownloadController" method="get">
            <%-- Hidden input to pass the image data or a reference to it --%>
            <%-- For simplicity, we'll assume DownloadController will fetch the image from session or a temp location --%>
            <input type="hidden" name="downloadType" value="encodedImage">
            <button class="btn px-8" type="submit">Save Encoded Image</button>
        </form>
      </div>
      <% } %>

      <p class="text-center text-xs text-[#33ff33]">2025 ZYPHER | ROOT ACCESS</p>
    </div>
  </div>

  <script>
    function setFormAction(action) {
      const form = document.getElementById('steganoForm');
      const formActionInput = document.getElementById('formAction');
      
      formActionInput.value = action;
      form.action = action === 'embed' ? 'embed' : 'extract';
      form.submit();
    }

    
  </script>

</body>
</html>