package app

import app.Summary.SmmryResponse

class SummarySpec extends UnitSpec {
  
  def fixture = new {
    val json = """{"sm_api_error":1,"sm_api_message":"INVALID API KEY"}"""
    val err = SmmryResponse(
      sm_api_error = Some(1),
      sm_api_message = Some("INVALID API KEY")
    )

    val json2 = """{"sm_api_title":"content title","sm_api_content":"the quick brown fox jumped over the lazy dog","sm_api_character_count":100}"""
    val success = SmmryResponse(
      sm_api_title = Some("content title"),
      sm_api_content = Some("the quick brown fox jumped over the lazy dog"),
      sm_api_character_count = Some(100)
    )
  }
  
  "Summary" should "parse json" in {
    val f = fixture
    import f._
    import Summary.MyJsonProtocol._
    import spray.json._
    json.parseJson.convertTo[SmmryResponse] should be (err)
    json2.parseJson.convertTo[SmmryResponse] should be (success)
  }

}
