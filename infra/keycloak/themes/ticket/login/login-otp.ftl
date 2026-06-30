<!DOCTYPE html>
<html lang="tr">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>2FA Dogrulama - Ticket Destek Portali</title>
  <link rel="stylesheet" href="${url.resourcesPath}/css/ticket-login.css">
</head>
<body>
  <main class="ticket-login-page">
    <section class="ticket-login-card" aria-labelledby="ticket-otp-title">
      <div class="ticket-brand" aria-label="Ticket Destek Portali">
        <div class="ticket-brand-title">Ticket<span class="ticket-brand-dot"></span></div>
        <div class="ticket-brand-subtitle">Destek Portali</div>
      </div>

      <div class="ticket-heading">
        <h1 id="ticket-otp-title">Iki adimli dogrulama</h1>
        <p>Authenticator uygulamanizdaki 6 haneli kodu girin.</p>
      </div>

      <#if message?has_content>
        <div class="ticket-alert" role="alert">
          ${kcSanitize(message.summary)?no_esc}
        </div>
      </#if>

      <form id="kc-otp-login-form" action="${url.loginAction}" method="post">
        <#if otpLogin?? && otpLogin.userOtpCredentials?? && (otpLogin.userOtpCredentials?size gt 1)>
          <div class="ticket-field">
            <span class="ticket-label">Dogrulama cihazi</span>
            <div class="ticket-radio-list">
              <#list otpLogin.userOtpCredentials as otpCredential>
                <label class="ticket-credential-option" for="kc-otp-credential-${otpCredential?index}">
                  <input
                    id="kc-otp-credential-${otpCredential?index}"
                    name="selectedCredentialId"
                    type="radio"
                    value="${otpCredential.id}"
                    <#if otpLogin.selectedCredentialId?? && otpCredential.id == otpLogin.selectedCredentialId>checked</#if>
                  >
                  <span>${otpCredential.userLabel!"Authenticator"}</span>
                </label>
              </#list>
            </div>
          </div>
        <#elseif otpLogin?? && otpLogin.selectedCredentialId??>
          <input type="hidden" name="selectedCredentialId" value="${otpLogin.selectedCredentialId}">
        </#if>

        <div class="ticket-field">
          <label class="ticket-label" for="otp">DOGRULAMA KODU</label>
          <input
            class="ticket-input"
            id="otp"
            name="otp"
            type="text"
            inputmode="numeric"
            pattern="[0-9]*"
            autocomplete="one-time-code"
            placeholder="000000"
            autofocus
          >
          <p class="ticket-helper">Kod her 30 saniyede bir yenilenir.</p>
        </div>

        <button class="ticket-primary-button" id="kc-login" name="login" type="submit">
          Dogrula
        </button>
      </form>
    </section>
  </main>
</body>
</html>
