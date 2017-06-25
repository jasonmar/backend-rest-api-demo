package app

import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model._

object OAuth {
  def authUri(clientId: String, scope: String, redirectUri: String) = s"https://login.live.com/oauth20_authorize.srf?client_id=$clientId&scope=$scope&redirect_uri=$redirectUri&display=popup&response_type=code"
  def tokenUri(clientId: String, clientSecret: String, redirectUri: String, authCode: String) = s"https://login.live.com/oauth20_token.srf?client_id=$clientId&redirect_uri=$redirectUri&client_secret=$clientSecret&code=$authCode&grant_type=authorization_code"

  // Get Client ID
  // https://apps.dev.microsoft.com

  // Documentation
  // https://developer.microsoft.com/en-us/graph/docs/concepts/auth_overview

  // Endpoints
  // https://login.microsoftonline.com/common/oauth2/v2.0/authorize
  // https://login.microsoftonline.com/common/oauth2/v2.0/token
  val FORM_CONTENT_TYPE: ContentType.WithCharset = MediaTypes.`application/x-www-form-urlencoded`.toContentType(HttpCharsets.`UTF-8`)

  // https://msdn.microsoft.com/en-us/library/hh243647.aspx#flows
  case class OAuth20Request(
    client_id: String, //The app's client ID.
    client_secret: String, // The app's client secret.
    grant_type: String, // The authorization type that the server returns. Valid values are "authorization_code" or "refresh_token".
    locale: Option[String], // Optional. A market string that determines how the consent UI is localized. If the value of this parameter is missing or is not valid, a market value is determined by using an internal algorithm.
    redirect_uri: String, // Equivalent to the endpoint that is described in the OAuth 2.0 spec.
    response_type: String, // The type of data to be returned in the response from the authorization server. Valid values are "code" or "token".
    scope: String, // Equivalent to the scope parameter that is described in the OAuth 2.0 spec.
    state: String, // Equivalent to the state parameter that is described in the OAuth 2.0 spec.
    display: String // The display type to be used for the authorization page. Valid values are "popup", "touch", "page", or "none".
  ) {
    def formData = {
      import akka.http.scaladsl.client.RequestBuilding._

      //val formData = Multipart.FormData(BodyPart("lol", HttpEntity(MediaTypes.`application/pdf`, new File("example.txt"))))

    }
  }

  case class OAuth20Response(
    access_token: String, // Equivalent to the access_token parameter that is described in the OAuth 2.0 spec.
    authentication_token: String, // The app's authentication token.
    code: String, // Equivalent to the code parameter that is described in the OAuth 2.0 spec.
    expires_in: String, // Equivalent to the expires_in parameter that is described in the OAuth 2.0 spec.
    refresh_token: String, // Equivalent to the refresh_token parameter that is described in the OAuth 2.0 spec.
    scope: String, // Equivalent to the scope parameter that is described in the OAuth 2.0 spec.
    state: String, // Equivalent to the state parameter that is described in the OAuth 2.0 spec.
    token_type: String // The type of data to be returned in the response from the authorization server.
  )

}
