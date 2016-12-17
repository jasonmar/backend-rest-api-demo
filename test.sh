#!/bin/sh

endpoint="http://localhost:8080"
curl_opts="-ivk"

fn_get(){
  uri=$1
  echo
  echo "GET ${uri}"
  echo
  curl ${curl_opts} "${uri}" 2>/dev/null
  echo
}

fn_post(){
  echo
  echo "POST ${uri} $@"
  echo
  echo "$@" | curl ${curl_opts} -d @- "${uri}" --header "Content-Type:application/json" 2>/dev/null
  echo
}

fn_post_id(){
  path="/id"
  uri="${endpoint}${path}"
  fn_post $@
}

fn_post_doc(){
  path="/doc"
  uri="${endpoint}${path}"
  fn_post $@
}

fn_get_id(){
  path="/id/$1"
  uri="${endpoint}${path}"
  fn_get $uri
}

fn_get_doc(){
  path="/doc/$1/$2"
  uri="${endpoint}${path}"
  fn_get $uri
}

fn_get_ids(){
  path="/id"
  uri="${endpoint}${path}"
  fn_get $uri
}

fn_get_docs(){
  path="/doc"
  uri="${endpoint}${path}"
  fn_get $uri
}

fn_title(){
  echo 
  echo
  echo
  echo
  echo "  # Test:"
  echo
  echo "  $@"
  echo
}


fn_title "Should get 404"
fn_get_id abc

fn_title "Should post Id abc"
fn_post_id '{"par": "abc"}'

fn_title "Should retrieve Id abc JSON"
fn_get_id abc

fn_title "Should post Document A"
fn_post_doc '{"par": "abc", "num":123, "pay":"def"}'

fn_title "Should retrieve Document A JSON"
fn_get_doc abc 123

fn_title "Should get 400 Bad Request"
fn_post_doc '{"par": "xyz", "num":987, "pay":"tuv"}'

fn_title "Should post Id xyz"
fn_post_id '{"par": "xyz"}'

fn_title "Should post Document B"
fn_post_doc '{"par": "xyz", "num":987, "pay":"tuv"}'


fn_title "Should list Ids A,B JSON"
fn_get_ids

fn_title "Should list Documents A,B JSON"
fn_get_docs


fn_title "Should post many Documents"
curl_opts="-q"
for id in aaa bbb ccc ddd eee fff ggg hhh iii jjj; do
  for x in 111 222 333 444 555; do
  	fn_post_id "{\"par\":\"${id}\"}" >/dev/null
    fn_post_doc "{\"par\": \"${id}\", \"num\":${x}, \"pay\":\"${id} ${x}\"}" >/dev/null
  done
  echo -n "."
done

fn_title "Should list many Documents"
fn_get_docs


fn_title "Should print one Id JSON"
echo '{ "par": {"S": "abc"} }' > key1.json
aws dynamodb get-item --endpoint-url http://localhost:8000 --table-name ids --key file://key1.json

fn_title "Should print document abc JSON"
echo '{ "par": {"S": "abc"}, "num": {"N": "123"} }' > key2.json
aws dynamodb get-item --endpoint-url http://localhost:8000 --table-name docs --key file://key2.json

fn_title "Should print all documents"
aws dynamodb scan --endpoint-url http://localhost:8000 --table-name docs
