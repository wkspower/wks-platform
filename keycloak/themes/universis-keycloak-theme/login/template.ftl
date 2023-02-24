<#macro registrationLayout bodyClass="" displayInfo=false displayMessage=true displayWide=false>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" class="${properties.kcHtmlClass!}">

<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">

    <#if properties.meta?has_content>
        <#list properties.meta?split(' ') as meta>
            <meta name="${meta?split('==')[0]}" content="${meta?split('==')[1]}"/>
        </#list>
    </#if>
    <title>${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico" />
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/css/${style}" rel="stylesheet" />
        </#list>
    </#if>
    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
    <#if scripts??>
        <#list scripts as script>
            <script src="${script}" type="text/javascript"></script>
        </#list>
    </#if>
</head>

<body class="${properties.kcBodyClass!}">

  <nav class="navbar navbar-top fixed-top navbar-expand-sm">
      <div class="collapse navbar-collapse" id="navbarCollapse">
        <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
          <ul class="navbar-nav ml-auto">
            <#list locale.supported as l>
              <#if l.languageTag != locale.currentLanguageTag>
                <li class="nav-item">
                    <a class="nav-link" href="${l.url}">${l.languageTag}</a>
                </li>
              </#if>
            </#list>
          </ul>
          </#if>
      </div>
  </nav>

  <main class="container-fluid">
      <div>
          <div class="row">
              <div class="col-lg-6">
                  <div class="mt-7">
                    <div class="row justify-content-center">
                        <div class="col-md-9">
                            <div class="card-group">
                                <div class="card border-0">
                                    <div class="card-body">
                                    <!--start kcLoginClass-->
                                    <div class="${properties.kcLoginClass!}">
                                      <div id="kc-header" class="${properties.kcHeaderClass!}">
                                        <div id="kc-header-wrapper" class="${properties.kcHeaderWrapperClass!}">${kcSanitize(msg("loginTitleHtml",(realm.displayNameHtml!'')))?no_esc}</div>
                                      </div>
                                      <div class="${properties.kcFormCardClass!} <#if displayWide>${properties.kcFormCardAccountClass!}</#if>">
                                        <header class="d-none ${properties.kcFormHeaderClass!}">
                                          <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                                              <div id="kc-locale">
                                                  <div id="kc-locale-wrapper" class="${properties.kcLocaleWrapperClass!}">
                                                      <div class="kc-dropdown" id="kc-locale-dropdown">
                                                          <a href="#" id="kc-current-locale-link">${locale.current}</a>
                                                          <ul>
                                                              <#list locale.supported as l>
                                                                  <li class="kc-dropdown-item"><a href="${l.url}">${l.label}</a></li>
                                                              </#list>
                                                          </ul>
                                                      </div>
                                                  </div>
                                              </div>
                                          </#if>
                                          <h1 id="kc-page-title"><#nested "header"></h1>
                                        </header>
                                        <div id="kc-content">
                                          <div id="kc-content-wrapper">

                                            <#-- App-initiated actions should not see warning messages about the need to complete the action -->
                                            <#-- during login.                                                                               -->
                                            <#if displayMessage && message?has_content && (message.type != 'warning' || !isAppInitiatedAction??)>
                                                <div class="alert alert-${message.type}">
                                                    <#if message.type = 'success'><span class="${properties.kcFeedbackSuccessIcon!}"></span></#if>
                                                    <#if message.type = 'warning'><span class="${properties.kcFeedbackWarningIcon!}"></span></#if>
                                                    <#if message.type = 'error'><span class="${properties.kcFeedbackErrorIcon!}"></span></#if>
                                                    <#if message.type = 'info'><span class="${properties.kcFeedbackInfoIcon!}"></span></#if>
                                                    <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                                                </div>
                                            </#if>

                                            <#nested "form">

                                            <#if auth?has_content && false >
                                            <form id="kc-select-back-form" action="${url.loginAction}" method="post" <#if displayWide>class="${properties.kcContentWrapperClass!}"</#if>>
                                                <div <#if displayWide>class="${properties.kcFormSocialAccountContentClass!} ${properties.kcFormSocialAccountClass!}"</#if>>
                                                    <div class="${properties.kcFormGroupClass!}">
                                                      <input class="${properties.kcButtonClass!} ${properties.kcButtonDefaultClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                                                             name="back" id="kc-back" type="submit" value="${msg("doBack")}"/>
                                                    </div>
                                                </div>
                                            </form>
                                            </#if>

                                            <#if displayInfo>
                                                <div id="kc-info" class="${properties.kcSignUpClass!}">
                                                    <div id="kc-info-wrapper" class="${properties.kcInfoAreaWrapperClass!}">
                                                        <#nested "info">
                                                    </div>
                                                </div>
                                            </#if>
                                          </div>
                                        </div>

                                      </div>
                                    </div>
                                    <!--end kcLoginClass-->
                                  </div>
                                </div>
                            </div>
                        </div>
                      </div>
                  </div>
              </div>
              <div class="col-lg-6 d-none d-xl-block d-lg-block">
                  <div class="row justify-content-center mt-7 text-white">
                      <div class="col-8 col-sm-6">
                          <div class="doc-block">
                            <#if realm.internationalizationEnabled  && locale.supported?size gt 1>
                              <#if locale.current == 'el'>
                                <#include "${'./welcome-message_' + locale.currentLanguageTag + '.ftl'}">
                              <#else>
                                <#include "./welcome-message_en.ftl">
                              </#if>
                            <#else>
                              <#include "./welcome-message_en.ftl">
                            </#if>
                          </div>
                      </div>
                  </div>
              </div>
          </div>
      </div>
  </main>


  <footer class="footer">
      <div class="container-fluid">
          <nav class="navbar navbar-expand-sm">
              <a class="navbar-brand" href="#">IT CENTER - STUDENT INFORMATION SYSTEM</a>
              <div class="collapse navbar-collapse" id="navbarCollapse">
                  <ul class="navbar-nav ml-auto">
                      <li class="nav-item">
                          <a class="nav-link text-uppercase" href="#">${msg("supportLink")}</a>
                      </li>
                      <li class="nav-item">
                          <a class="nav-link text-uppercase" href="#">${msg("statisticsLink")}</a>
                      </li>
                  </ul>
              </div>
          </nav>
      </div>
  </footer>
</body>
</html>
</#macro>
