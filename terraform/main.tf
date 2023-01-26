terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "<= 3.42.0"
    }
  }
}

provider "aws" {
  region = "eu-west-1"
}


# User Guide

resource "aws_s3_bucket" "wks-userguide-bucket" {
  bucket        = "wks-userguide-bucket"
  acl           = "private"
  force_destroy = true

  tags = {
    Name = "my-react-bucket"
  }

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "block_public_access_userguide" {
  bucket = aws_s3_bucket.wks-userguide-bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

data "aws_iam_policy_document" "react_app_s3_policy_userguide" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.wks-userguide-bucket.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.oai_userguide.iam_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "react_app_bucket_policy" {
  bucket = aws_s3_bucket.wks-userguide-bucket.id
  policy = data.aws_iam_policy_document.react_app_s3_policy_userguide.json
}


# Case Portal Admin

resource "aws_s3_bucket" "wks-caseportaladmin-bucket" {
  bucket        = "wks-caseportaladmin-bucket"
  acl           = "private"
  force_destroy = true

  tags = {
    Name = "my-react-bucket"
  }

  versioning {
    enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "block_public_access_caseportaladmin" {
  bucket = aws_s3_bucket.wks-caseportaladmin-bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

data "aws_iam_policy_document" "react_app_s3_policy_caseportaladmin" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.wks-caseportaladmin-bucket.arn}/*"]

    principals {
      type        = "AWS"
      identifiers = [aws_cloudfront_origin_access_identity.oai_caseportaladmin.iam_arn]
    }
  }
}

resource "aws_s3_bucket_policy" "react_app_bucket_policy_caseportaladmin" {
  bucket = aws_s3_bucket.wks-caseportaladmin-bucket.id
  policy = data.aws_iam_policy_document.react_app_s3_policy_caseportaladmin.json
}