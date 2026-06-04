<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Giris Yap - Ticket Destek Portali</title>
  <link rel="stylesheet" href="${url.resourcesPath}/css/ticket-login.css">
</head>
<body>
  <main class="ticket-login-page">
    <section class="ticket-login-card" aria-labelledby="ticket-login-title">
      <div class="ticket-brand" aria-label="Ticket Destek Portali">
        <div class="ticket-brand-title">Ticket<span class="ticket-brand-dot"></span></div>
        <div class="ticket-brand-subtitle">Destek Portali</div>
      </div>

      <div class="ticket-heading">
        <h1 id="ticket-login-title">Hos geldiniz</h1>
        <p>Devam etmek icin giris yapin</p>
      </div>

      <#if message?has_content>
        <div class="ticket-alert" role="alert">
          ${kcSanitize(message.summary)?no_esc}
        </div>
      </#if>

      <form id="kc-form-login" action="${url.loginAction}" method="post">
        <div class="ticket-field">
          <label class="ticket-label" for="username">
            <#if !realm.loginWithEmailAllowed>Kullanici adi<#else>E-posta</#if>
          </label>
          <input
            class="ticket-input"
            id="username"
            name="username"
            type="text"
            value="${(login.username!'')}"
            autocomplete="username"
            placeholder="ornek@firma.com"
            autofocus
            <#if usernameEditDisabled??>disabled</#if>
          >
        </div>

        <div class="ticket-field">
          <label class="ticket-label" for="password">Sifre</label>
          <div class="ticket-password-field">
            <input
              class="ticket-input"
              id="password"
              name="password"
              type="password"
              autocomplete="current-password"
              placeholder="........"
            >
            <button class="ticket-password-toggle" type="button" aria-label="Sifreyi goster veya gizle" data-password-toggle>
              <svg aria-hidden="true" viewBox="0 0 24 24" fill="none">
                <path d="M3 12s3.2-6 9-6 9 6 9 6-3.2 6-9 6-9-6-9-6Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
                <path data-eye-slash d="M4 4l16 16" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
              </svg>
            </button>
          </div>
        </div>

        <div class="ticket-options">
          <#if realm.rememberMe && !usernameEditDisabled??>
            <label class="ticket-checkbox" for="rememberMe">
              <input
                id="rememberMe"
                name="rememberMe"
                type="checkbox"
                <#if login.rememberMe??>checked</#if>
              >
              <span>Beni hatirla</span>
            </label>
          <#else>
            <span></span>
          </#if>

          <#if realm.resetPasswordAllowed>
            <a class="ticket-link" href="${url.loginResetCredentialsUrl}">Sifremi unuttum</a>
          </#if>
        </div>

        <#if auth?has_content && auth.selectedCredential?has_content>
          <input type="hidden" name="credentialId" value="${auth.selectedCredential}">
        </#if>

        <button class="ticket-primary-button" id="kc-login" name="login" type="submit">
          Giris Yap
        </button>
      </form>

      <div class="ticket-divider">veya</div>

      <div class="ticket-register">
        <#if realm.registrationAllowed && url.registrationUrl??>
          <span>Hesabiniz yok mu?</span>
          <a class="ticket-link" href="${url.registrationUrl}">Kayit olun</a>
        <#else>
          <span>Hesap islemleri icin kurum yoneticinizle iletisime gecin.</span>
        </#if>
      </div>
    </section>
  </main>

  <script>
    (function () {
      var toggle = document.querySelector("[data-password-toggle]");
      var password = document.getElementById("password");
      var slash = document.querySelector("[data-eye-slash]");

      if (!toggle || !password || !slash) {
        return;
      }

      toggle.addEventListener("click", function () {
        var isHidden = password.getAttribute("type") === "password";
        password.setAttribute("type", isHidden ? "text" : "password");
        slash.style.display = isHidden ? "none" : "block";
      });
    })();
  </script>
</body>
</html>
