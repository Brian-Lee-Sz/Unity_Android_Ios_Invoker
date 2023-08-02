//
//  UnityMessageManager.m
//  Unity-iPhone
//
//

#import "UnityMessageManager.h"
@interface UnityMessageManager()
@property (nonatomic, assign)BOOL isXX;
@property (nonatomic, strong)NSDictionary *doSomething;
@end
@implementation UnityMessageManager
static UnityMessageManager *_instance = nil;


+ (instancetype)sharedInstance{
    static dispatch_once_t _onceToken;
    dispatch_once(&_onceToken, ^{
        _instance = [[UnityMessageManager alloc]init];
    });
    return _instance;
}


+ (void)sendMessageToUnity:(NSString *)methodName params:(NSString *)params{
    const char *methodCharName = [UnityMessageManager cMsgToUnityMsg:methodName];
    const char *charParams     = [UnityMessageManager cMsgToUnityMsg:params];
    UnitySendMessage("ScriptObj",methodCharName,charParams);
}

+ (const char *)cMsgToUnityMsg:(NSString *)msg{
    char *cMsg = (char *)malloc(strlen(msg.UTF8String)+1);
    strcpy(cMsg, msg.UTF8String);
    return cMsg;
}
+ (NSString *)fetchStringParam:(NSDictionary *)dict{
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingFragmentsAllowed error:nil];
    return [[NSString alloc]initWithData:data encoding:NSUTF8StringEncoding];
}
+ (NSDictionary *)dictionaryWithJsonStr:(const char *)str{
    NSString *jsonString = [NSString stringWithUTF8String:str];
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    return [NSJSONSerialization JSONObjectWithData:jsonData options:NSJSONReadingAllowFragments error:nil];
}

//外部调用
extern "C"
{
    void initGame(int enviroment){
//sdk初始化
    }
}
@end
