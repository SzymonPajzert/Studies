; ModuleID = 'external.c'
target datalayout = "e-m:e-i64:64-f80:128-n8:16:32:64-S128"
target triple = "x86_64-pc-linux-gnu"

%struct._IO_FILE = type { i32, i8*, i8*, i8*, i8*, i8*, i8*, i8*, i8*, i8*, i8*, i8*, %struct._IO_marker*, %struct._IO_FILE*, i32, i32, i64, i16, i8, [1 x i8], i8*, i64, i8*, i8*, i8*, i8*, i64, i32, [20 x i8] }
%struct._IO_marker = type { %struct._IO_marker*, %struct._IO_FILE*, i32 }

@stdin = external global %struct._IO_FILE*, align 8

; Function Attrs: nounwind uwtable
define i8* @string_concat(i8* %a, i8* %b) #0 {
  %1 = alloca i8*, align 8
  %2 = alloca i8*, align 8
  %a_len = alloca i64, align 8
  %b_len = alloca i64, align 8
  %result = alloca i8*, align 8
  store i8* %a, i8** %1, align 8
  store i8* %b, i8** %2, align 8
  %3 = load i8*, i8** %1, align 8
  %4 = call i64 @strlen(i8* %3) #4
  store i64 %4, i64* %a_len, align 8
  %5 = load i8*, i8** %2, align 8
  %6 = call i64 @strlen(i8* %5) #4
  store i64 %6, i64* %b_len, align 8
  %7 = load i64, i64* %a_len, align 8
  %8 = load i64, i64* %b_len, align 8
  %9 = add i64 %7, %8
  %10 = add i64 %9, 1
  %11 = call noalias i8* @malloc(i64 %10) #5
  store i8* %11, i8** %result, align 8
  %12 = load i8*, i8** %result, align 8
  %13 = load i8*, i8** %1, align 8
  %14 = call i8* @strcpy(i8* %12, i8* %13) #5
  %15 = load i8*, i8** %result, align 8
  %16 = load i64, i64* %a_len, align 8
  %17 = getelementptr inbounds i8, i8* %15, i64 %16
  %18 = load i8*, i8** %2, align 8
  %19 = call i8* @strcpy(i8* %17, i8* %18) #5
  %20 = load i64, i64* %a_len, align 8
  %21 = load i64, i64* %b_len, align 8
  %22 = add i64 %20, %21
  %23 = load i8*, i8** %result, align 8
  %24 = getelementptr inbounds i8, i8* %23, i64 %22
  store i8 0, i8* %24, align 1
  %25 = load i8*, i8** %result, align 8
  ret i8* %25
}

; Function Attrs: nounwind readonly
declare i64 @strlen(i8*) #1

; Function Attrs: nounwind
declare noalias i8* @malloc(i64) #2

; Function Attrs: nounwind
declare i8* @strcpy(i8*, i8*) #2

; Function Attrs: nounwind uwtable
define i8* @readString() #0 {
  %line = alloca i8*, align 8
  %len = alloca i64, align 8
  %read = alloca i64, align 8
  store i8* null, i8** %line, align 8
  store i64 0, i64* %len, align 8
  br label %1

; <label>:1                                       ; preds = %5, %0
  %2 = load %struct._IO_FILE*, %struct._IO_FILE** @stdin, align 8
  %3 = call i64 @getline(i8** %line, i64* %len, %struct._IO_FILE* %2)
  store i64 %3, i64* %read, align 8
  %4 = icmp sle i64 %3, 1
  br i1 %4, label %5, label %6

; <label>:5                                       ; preds = %1
  store i64 0, i64* %len, align 8
  br label %1

; <label>:6                                       ; preds = %1
  %7 = load i64, i64* %read, align 8
  %8 = sub nsw i64 %7, 1
  %9 = load i8*, i8** %line, align 8
  %10 = getelementptr inbounds i8, i8* %9, i64 %8
  store i8 0, i8* %10, align 1
  %11 = load i8*, i8** %line, align 8
  ret i8* %11
}

declare i64 @getline(i8**, i64*, %struct._IO_FILE*) #3

attributes #0 = { nounwind uwtable "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+fxsr,+mmx,+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { nounwind readonly "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+fxsr,+mmx,+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #2 = { nounwind "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+fxsr,+mmx,+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #3 = { "disable-tail-calls"="false" "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "target-cpu"="x86-64" "target-features"="+fxsr,+mmx,+sse,+sse2" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #4 = { nounwind readonly }
attributes #5 = { nounwind }

!llvm.ident = !{!0}

!0 = !{!"clang version 3.8.0-2ubuntu4 (tags/RELEASE_380/final)"}
