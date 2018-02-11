package knockoff

trait StringExtras {
  implicit def KnockoffString(s: CharSequence): KnockoffString =
    new KnockoffString(s.toString)
}
