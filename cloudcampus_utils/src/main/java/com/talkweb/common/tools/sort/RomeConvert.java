package com.talkweb.common.tools.sort;
/**
 * @ClassName RomeConvert.java
 * @author liboqi
 * @version 1.0
 * @Description 罗马数字转换
 * @date 2015年12月18日 下午5:35:06
 */
public class RomeConvert {
	 private final int num;   //罗马数字转换后的阿拉伯数字（十进制）
     public RomeConvert(String roman) throws Exception 
     {
        if (roman.length() == 0)//输入为空
        {
      	  throw new Exception("不能输入空字符");
        }
        roman = roman.toUpperCase();//所有罗马数字都转换为大写
        int i = 0;//记录罗马数字每个字符的位置
        int arabic = 0;//转换后的阿拉伯数字
        while (i < roman.length()) 
        {
           char letter = roman.charAt(i);        // 罗马数字当前位置的字符
           int number = letterToNumber(letter);  // 字符转化为阿拉伯数字
           
           if (number < 0)
           {
          	 throw new Exception("罗马数字中不包含"+letter);
           }
           i++;//移动到字符串的下一个位置
           if (i == roman.length())   //罗马数字已处理完毕
           {
          	 arabic += number;
           }
           else 
           {
          	 	char nextLetter = roman.charAt(i);
          	 	int nextNumber = letterToNumber(nextLetter);
	                if (nextNumber > number)  //后边的字符比前边的大
	                {  
		                   int result = nextNumber - number;
		                   if(result == 4 || result == 9 || result == 40 || result == 90 || result == 400 || result == 900)
		                   {
		                	   arabic += result;
		                	   i++;
		                	   if(i == roman.length())     //罗马数字已处理完毕
		                	   {
		                		   break;
		                	   }
		                	   else
		                	   {
		                    	   char  afterNextLetter = roman.charAt(i);   
		                    	   int afterNextNumber = letterToNumber(afterNextLetter);
		                    	   
		                    	   if(afterNextNumber > result)
		                    	   {
		                    		   throw new Exception("不合法的罗马数字"+letter+nextLetter+afterNextLetter);
		                    	   }
		                	   }
		                   }
		                   else 
		                   {
		                	   throw new Exception("不合法的罗马数字"+letter+nextLetter);
		                   }
	                 }
	                 else 
	                 {
		                	if((number==5 || number==50 ||number==500)&& number == nextNumber)   //V、L、D用于大数右边（相加），使用超过1次。
			             	{
			             		throw new Exception("不合法的罗马数字"+letter+nextLetter);
			             	}
		                	if(number == nextNumber)   
		                	{
		                		i++;//还要再看下一个字符
		                		if (i == roman.length())//罗马数字已处理完毕
		                        {
		                           arabic += number+nextNumber;
		                           break;
		                        }
		                		char  afterNextLetter = roman.charAt(i);   
		                 	    int afterNextNumber = letterToNumber(afterNextLetter);
		                 	    if(afterNextNumber > nextNumber)//I、X、C在在大数左边（即相减时）使用超过2个
		                 	    {
		                 	    	throw new Exception("不合法的罗马数字"+letter+nextLetter+afterNextLetter);
		                 	    }
		                 	    else if(afterNextNumber == nextNumber)  //出现3个字符都相同的情况，如III
		                 	    {
		                 	    	i++;//还要再看下一个字符,可能会出现IIII这种情况（不允许的，应抛出异常）
		                 	    	if (i == roman.length())//罗马数字已处理完毕
			                        {
		                 	    		arabic += number+nextNumber+afterNextNumber;
			                            break;
			                        }
		                 	    	char  afterNextNextLetter = roman.charAt(i);   
			                 	    int afterNextNextNumber = letterToNumber(afterNextNextLetter);
			                 	    
		                 	    	if(afterNextNextNumber == afterNextNumber)//出现IIII这种情况
		                 	    	{
		                 	    		throw new Exception("不合法的罗马数字"+letter+nextLetter+afterNextLetter+afterNextNextLetter);
		                 	    	}
		                 	    	else 
		                 	    	{
		                 	    		arabic += number;
		                 	    		i=i-2;//回退2个字符（因为考虑了4个字符）
		                 	    	}
		                 	    }
		                 	    else 
		                 	    {
		                 	    	arabic += number+nextNumber;
		                 	    }
		                	}
			                else
			                {
			                	arabic += number;
			                }
	                 }
           }  
        } 
        if (arabic > 3999)
        {
           throw new Exception("输入的数字不能超过3999");
        }
        num = arabic;
     } 
     
  
     /**
      * 罗马字符转换为阿拉伯数字
      * @param letter  罗马字符
      * @return 正常罗马字符，返回阿拉伯数字；否则，返回-1
      */
    /* private int letterToNumber(char letter) 
     {
    	 
        switch (letter) 
        {
           case 'I':  return 1;
           case 'V':  return 5;
           case 'X':  return 10;
           case 'L':  return 50;
           case 'C':  return 100;
           case 'D':  return 500;
           case 'M':  return 1000;
           default:   return -1;
           
        }
     }*/
     
    /**
      * 罗马字符转换为阿拉伯数字
      * @param letter  罗马字符
      * @return 正常罗马字符，返回阿拉伯数字；否则，返回-1
      */
     private int letterToNumber(char letter) 
     {
        switch (letter) 
        {
           case 'Ⅰ': return 1;
           case 'Ⅱ': return 2;
           case 'Ⅲ': return 3;
           case 'Ⅳ': return 4;
           case 'Ⅴ': return 5;
           case 'Ⅵ': return 6;
           case 'Ⅶ': return 7;
           case 'Ⅷ': return 8;
           case 'Ⅸ': return 9;
           case 'Ⅹ': return 10;
           case 'Ⅺ': return 11;
           case 'Ⅻ': return 12;
           case 'Ⅼ': return 50;
           case 'Ⅽ': return 100;
           case 'Ⅾ': return 500;
           case 'Ⅿ': return 1000;
           default:  return -1;
        }
     }

	public int getNum() 
	{
		return num;
	}
}
